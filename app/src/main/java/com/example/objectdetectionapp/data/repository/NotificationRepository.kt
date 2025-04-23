package com.example.objectdetectionapp.data.repository

import android.util.Log
import com.example.objectdetectionapp.data.firebase.FCMService
import com.example.objectdetectionapp.data.firebase.FirebaseService

class NotificationRepository(
    private val fcmService: FCMService,
    private val firebaseService: FirebaseService
) {
    suspend fun sendNotificationToOverlookers(
        surveillanceUUID: String,
        title: String = "Alert from Surveillance",
        body: String = "Hi you are connected"
    ) {
        val tokens = firebaseService.getOverlookerFCMTokens(surveillanceUUID)
        tokens.forEach { token ->
            fcmService.sendNotificationToToken(
                token = token,
                title = title,
                body = body
            )
        }
        Log.w(
            "NotificationRepo",
            "Tokens sent to fcmService.sendNotificationToToken()"
        )

    }

    suspend fun sendPairingNotificationToSurveillance(
        surveillanceUUID: String,
        overlookerUUID: String
    ) {
        val token = firebaseService.getSurveillanceFCMToken(surveillanceUUID)

        if (!token.isNullOrEmpty()) {
            fcmService.sendNotificationToToken(
                token = token,
                title = "You are Paired! 🔗",
                body = "Hi, you are connected to:\nUUID: $overlookerUUID"
            )
        } else {
            Log.w(
                "NotificationRepo",
                "Surveillance FCM token is null or empty for $surveillanceUUID"
            )
        }
    }

    suspend fun checkIfFCMTokenAreSavedInDB(uuid: String): Boolean {
        return try {
            firebaseService.checkIfFCMTokenExists(uuid)
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error checking if FCM Token exists for $uuid: ${e.message}")
            false
        }
    }
}
