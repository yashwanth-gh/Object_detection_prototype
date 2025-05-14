package com.example.objectdetectionapp.data.firebase

import android.util.Log
import com.example.objectdetectionapp.data.models.BoundingBox
import com.example.objectdetectionapp.data.models.Detection
import com.example.objectdetectionapp.data.models.Overlooker
import com.example.objectdetectionapp.data.models.SurveillanceDevice
import com.example.objectdetectionapp.data.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseServiceImpl : FirebaseService {

    private val database = FirebaseDatabase.getInstance().reference
    private val _tag = "FirebaseServiceImpl"

    override suspend fun saveSurveillanceDevice(uuid: String, user: User) {
        try {
            // Generate the pairing code from the first 6 characters of the UUID (you can adjust this)
            val pairingCode = uuid.take(6)

            // Save the Surveillance device data, including pairing code
            val deviceData = mapOf(
                "status" to "active",
                "pairingCode" to pairingCode,
                "user" to mapOf(
                    "username" to user.username,
                    "email" to user.email
                )
            )
            database.child("surveillance_devices")
                .child(uuid)
                .setValue(deviceData)
                .await()
            Log.d(_tag, "UUID $uuid and user data saved to Firebase")

        } catch (e: Exception) {
            Log.e(_tag, "Failed to save UUID and user data: ${e.message}")
            throw e
        }
    }

    override suspend fun getTokenAndSaveToDatabase(uuid: String) {
        try {
            val token =
                FirebaseMessaging.getInstance().token.await() // from kotlinx-coroutines-play-services
            database.child("fcm_tokens")
                .child(uuid)
                .setValue(token).await()

            Log.d("FCMToken", "✅ FCM Token saved for $uuid")
        } catch (e: Exception) {
            Log.e("FCMToken", "❌ Failed to save token: ${e.message}")
        }
    }


    override suspend fun isValidSurveillanceUUID(uuid: String): Boolean {
        return try {
            val snapshot = database.child("surveillance_devices").child(uuid).get().await()
            snapshot.exists() && snapshot.child("status").value == "active"
        } catch (e: Exception) {
            Log.e(_tag, "UUID validation failed: ${e.message}")
            false
        }
    }

    override suspend fun addOverlookerToSurveillance(
        surveillanceUUID: String,
        overlookerUUID: String,
        user: User
    ) {
        try {
            val path = database.child("surveillance_devices")
                .child(surveillanceUUID)
                .child("overlookers")
                .child(overlookerUUID)

            path.setValue(user).await()
            Log.d(_tag, "Overlooker $overlookerUUID added under $surveillanceUUID")
        } catch (e: Exception) {
            Log.e(_tag, "Failed to add Overlooker: ${e.message}")
            throw e
        }
    }

    override suspend fun getOverlookerFCMTokens(surveillanceUUID: String): List<String> {
        return try {
            val tokens = mutableListOf<String>()
            val overlookersSnap = FirebaseDatabase.getInstance()
                .getReference("surveillance_devices/$surveillanceUUID/overlookers")
                .get().await()

            for (overlooker in overlookersSnap.children) {
                val overlookerUUID = overlooker.key ?: continue
                val tokenSnap = FirebaseDatabase.getInstance()
                    .getReference("fcm_tokens/$overlookerUUID")
                    .get().await()
                val token = tokenSnap.getValue(String::class.java)
                if (!token.isNullOrEmpty()) {
                    Log.w(
                        _tag,
                        "your token is : $token"
                    )
                    tokens.add(token)
                } else {
                    Log.w(
                        _tag,
                        "Token is empty or null"
                    )
                }
            }

            tokens
        } catch (e: Exception) {
            Log.e("FirebaseServiceImpl", "Failed to get tokens: ${e.message}")
            emptyList()
        }
    }

    override suspend fun getSurveillanceFCMToken(surveillanceUUID: String): String? {
        val snapshot = FirebaseDatabase.getInstance()
            .getReference("fcm_tokens/$surveillanceUUID")
            .get()
            .await()

        return snapshot.getValue(String::class.java)
    }

    override suspend fun fetchFullSurveillanceUUID(pairingCode: String): String? {
        return try {
            // Query Firebase to fetch the full UUID using the pairing code
            val snapshot = database
                .child("surveillance_devices")
                .orderByChild("pairingCode")
                .equalTo(pairingCode)
                .get()
                .await()

            // Check if the snapshot has data
            if (snapshot.exists()) {
                // Get the full UUID (assuming the pairingCode is unique)
                val uuid = snapshot.children.firstOrNull()?.key
                if (uuid != null) {
                    return uuid
                } else {
                    Log.e(_tag, "No UUID found for pairing code: $pairingCode")
                    null
                }
            } else {
                null // Return null if no device is found with the given pairing code
            }
        } catch (e: Exception) {
            Log.e(_tag, "Error fetching full UUID: ${e.message}")
            null
        }
    }

    override suspend fun checkIfFCMTokenExists(uuid: String): Boolean {
        return try {
            val snapshot = database.child("fcm_tokens").child(uuid).get().await()
            snapshot.exists()
        } catch (e: Exception) {
            Log.e(_tag, "Error checking if FCM token exists for $uuid: ${e.message}")
            false
        }
    }

    override suspend fun fetchDetectionDetails(surveillanceUUID: String): Flow<List<Detection>> =
        callbackFlow {
            val detectionsRef =
                database.child("surveillance_devices").child(surveillanceUUID).child("detections")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val detectionsList = mutableListOf<Detection>()
                    for (childSnapshot in snapshot.children) {
                        try {
                            val timestamp =
                                childSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                            val label =
                                childSnapshot.child("label").getValue(String::class.java) ?: ""
                            val confidence =
                                childSnapshot.child("confidence").getValue(Float::class.java) ?: 0f
                            val imagePath =
                                childSnapshot.child("imagePath").getValue(String::class.java)
                            val boundingBoxData =
                                childSnapshot.child("boundingBox").value as? Map<String, Long>
                            val boundingBox = BoundingBox(
                                x = boundingBoxData?.get("x")?.toInt() ?: 0,
                                y = boundingBoxData?.get("y")?.toInt() ?: 0,
                                width = boundingBoxData?.get("width")?.toInt() ?: 0,
                                height = boundingBoxData?.get("height")?.toInt() ?: 0
                            )
                            val uuid = childSnapshot.key ?: ""
                            detectionsList.add(
                                Detection(
                                    id = uuid,
                                    timestamp = timestamp,
                                    label = label,
                                    confidence = confidence,
                                    imagePath = imagePath,
                                    boundingBox = boundingBox
                                )
                            )
                        } catch (e: Exception) {
                            Log.e(_tag, "Error parsing detection: ${e.message}")
                        }
                    }
                    trySend(detectionsList.reversed()).isSuccess
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(_tag, "Failed to read detections: ${error.message}")
                    trySend(emptyList()).isSuccess
                }
            }
            detectionsRef.addValueEventListener(listener)
            awaitClose { detectionsRef.removeEventListener(listener) }
        }

    override suspend fun saveUser(user: User) {
        try {
            val usersRef = database.child("users")
            val uid = UUID.randomUUID().toString()
            usersRef.child(uid).setValue(user).await()
            Log.d(_tag, "User ${user.username} saved to Firebase")
        } catch (e: Exception) {
            Log.e(_tag, "Failed to save user to Firebase: ${e.message}")
            throw e
        }
    }

    override suspend fun getSurveillanceDevice(uuid: String): SurveillanceDevice? {
        return try {
            val snapshot = database.child("surveillance_devices").child(uuid).get().await()
            if (snapshot.exists()) {
                val status = snapshot.child("status").getValue(String::class.java) ?: "unknown"
                val pairingCode =
                    snapshot.child("pairingCode").getValue(String::class.java) ?: "N/A"

                val userSnapshot = snapshot.child("user")
                val user = User(
                    username = userSnapshot.child("username").getValue(String::class.java)
                        ?: "Unknown",
                    email = userSnapshot.child("email").getValue(String::class.java) ?: ""
                )

                val overlookersMap = mutableMapOf<String, Overlooker>()
                val overlookersSnapshot = snapshot.child("overlookers")
                for (child in overlookersSnapshot.children) {
                    val overlooker = child.getValue(Overlooker::class.java)
                    if (overlooker != null) {
                        overlookersMap[child.key ?: ""] = overlooker
                    }
                }

                SurveillanceDevice(
                    pairingCode = pairingCode,
                    status = status,
                    user = user,
                    overlookers = overlookersMap
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(_tag, "Error fetching surveillance device: ${e.message}")
            null
        }
    }

    override suspend fun getOverlookersForSurveillance(surveillanceUUID: String): List<Overlooker> {
        return try {
            val snapshot = database.child("surveillance_devices")
                .child(surveillanceUUID)
                .child("overlookers")
                .get()
                .await()

            val overlookers = mutableListOf<Overlooker>()
            for (child in snapshot.children) {
                val overlookerUUID = child.key ?: continue
                val username = child.child("username").getValue(String::class.java)
                val email = child.child("email").getValue(String::class.java)

                if (username != null && email != null) {
                    overlookers.add(
                        Overlooker(
                            uuid = overlookerUUID, // Include UUID
                            username = username,
                            email = email
                        )
                    )
                }
            }

            overlookers
        } catch (e: Exception) {
            Log.e("FirebaseService", "Error fetching overlookers: ${e.message}")
            emptyList()
        }
    }


    override suspend fun deleteOverlookerFromSurveillance(
        surveillanceUUID: String,
        overlookerUUID: String
    ) {
        try {
            database.child("surveillance_devices")
                .child(surveillanceUUID)
                .child("overlookers")
                .child(overlookerUUID)
                .removeValue()
                .await()
            Log.d(_tag, "Overlooker $overlookerUUID removed from surveillance $surveillanceUUID")
        } catch (e: Exception) {
            Log.e(_tag, "Error deleting overlooker: ${e.message}")
            throw e
        }
    }

    override suspend fun getOverlookerForSurveillanceDevice(
        surveillanceUUID: String,
        overlookerUUID: String
    ): Overlooker? {
        return try {
            val snapshot = database.child("surveillance_devices")
                .child(surveillanceUUID)
                .child("overlookers")
                .child(overlookerUUID)
                .get()
                .await()
            snapshot.getValue(Overlooker::class.java)
        } catch (e: Exception) {
            Log.e(
                _tag,
                "Error fetching overlooker $overlookerUUID for $surveillanceUUID: ${e.message}"
            )
            null
        }
    }

    override suspend fun deleteDetection(surveillanceUUID: String, detectionId: String) {
        try {
            database.child("surveillance_devices")
                .child(surveillanceUUID).child("detections")
                .child(detectionId).removeValue().await()
            Log.d(_tag, "Detection with ID $detectionId deleted from $surveillanceUUID")
        } catch (e: Exception) {
            Log.e(_tag, "Error deleting detection $detectionId: ${e.message}")
            throw e
        }
    }


}