package com.example.objectdetectionapp.data.firebase

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class FirebaseServiceImpl : FirebaseService {

    private val database = FirebaseDatabase.getInstance().reference
    private val _tag = "FirebaseServiceImpl"

    override suspend fun saveSurveillanceDevice(uuid: String) {
        try {
            // Generate the pairing code from the first 6 characters of the UUID (you can adjust this)
            val pairingCode = uuid.take(6)

            // Save the Surveillance device data, including pairing code
            val deviceData = mapOf(
                "status" to "active",
                "pairingCode" to pairingCode
            )
            database.child("surveillance_devices")
                .child(uuid)
                .setValue(deviceData)
                .await()
            Log.d(_tag, "UUID $uuid saved to Firebase")

        } catch (e: Exception) {
            Log.e(_tag, "Failed to save UUID: ${e.message}")
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
        overlookerUUID: String
    ) {
        try {
            val path = database.child("surveillance_devices")
                .child(surveillanceUUID)
                .child("overlookers")
                .child(overlookerUUID)

            path.setValue(true).await()
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

}