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
            database.child("surveillance_devices")
                .child(uuid)
                .setValue(mapOf("status" to "active"))
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
                    tokens.add(token)
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

}