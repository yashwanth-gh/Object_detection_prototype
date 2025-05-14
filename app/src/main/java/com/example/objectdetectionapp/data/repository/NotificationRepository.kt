package com.example.objectdetectionapp.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.example.objectdetectionapp.data.firebase.EmailServices
import com.example.objectdetectionapp.data.firebase.FCMService
import com.example.objectdetectionapp.data.firebase.FirebaseService
import com.example.objectdetectionapp.data.models.SurveillanceDevice
import com.example.objectdetectionapp.tflite.EfficientDetLiteDetector

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
        overlookerUUID: String,
        userName:String = "new user"
    ) {
        val token = firebaseService.getSurveillanceFCMToken(surveillanceUUID)

        if (!token.isNullOrEmpty()) {
            fcmService.sendNotificationToToken(
                token = token,
                title = "New device Paired! 🔗",
                body = "Hi, you are connected to $userName"
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

    suspend fun sendEmailReportToOverlookers(
        deviceData: SurveillanceDevice,
        personDetections: List<EfficientDetLiteDetector.DetectionResult>,
        image: Bitmap?
    ) {
        try {
            val overlookersEmails = deviceData.overlookers.values.mapNotNull { it.email }

            if (overlookersEmails.isEmpty()) {
                Log.w("NotificationRepo", "No overlooker emails found in device data")
                return
            }

            Log.w("NotificationRepo", "Attempting to send email")

            EmailServices.sendEmail(
                recipients = overlookersEmails,
                deviceData = deviceData,
                personDetections = personDetections,
                image = image
            )
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error sending email report: ${e.message}")
        }
    }



}
