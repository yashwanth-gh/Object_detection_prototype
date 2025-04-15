package com.example.objectdetectionapp.data.firebase

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStream
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


class FCMService(private val context: Context) {

    private val client = OkHttpClient()
    private val credentials: GoogleCredentials

    init {
        // Initialization is lightweight, so can stay in init.
        val stream: InputStream = context.assets.open("object-detection-app-prototype-39eb89e2d4d3.json")
        credentials = GoogleCredentials.fromStream(stream)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
    }

    suspend fun sendNotificationToToken(
        token: String,
        title: String,
        body: String
    ) = withContext(Dispatchers.IO) { // Switch to IO dispatcher for network operations
        try {
            credentials.refreshIfExpired() // Ensure token is fresh before making network request.
            val accessToken = credentials.accessToken.tokenValue

            val payload = JSONObject().apply {
                put("message", JSONObject().apply {
                    put("token", token)
                    put("notification", JSONObject().apply {
                        put("title", title)
                        put("body", body)
                    })
                })
            }

            val request = Request.Builder()
                .url("https://fcm.googleapis.com/v1/projects/object-detection-app-prototype/messages:send")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer $accessToken")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("FCMService", "❌ Failed to send FCM: ${response.code} ${response.body?.string()}")
                } else {
                    Log.d("FCMService", "✅ Notification sent: ${response.body?.string()}")
                }
            }
        } catch (e: Exception) {
            Log.e("FCMService", "Error sending notification: ${e.message}", e)
            // Handle the error appropriately, perhaps log it or show a message to the user.
        }
    }
}
