package com.example.objectdetectionapp.data.repository

import android.util.Log
import com.example.objectdetectionapp.data.firebase.FCMService
import com.example.objectdetectionapp.data.firebase.FirebaseService

class NotificationRepository(
    private val fcmService: FCMService,
    private val firebaseService: FirebaseService
) {
    suspend fun sendNotificationToOverlookers(surveillanceUUID: String) {
        val tokens = firebaseService.getOverlookerFCMTokens(surveillanceUUID)
        tokens.forEach { token ->
            fcmService.sendNotificationToToken(
                token = token,
                title = "Alert from Surveillance",
                body = "Hi you are connected to:\nUUID: $surveillanceUUID"
            )
        }
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
            Log.w("NotificationRepo", "Surveillance FCM token is null or empty for $surveillanceUUID")
        }
    }
}
