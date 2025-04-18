package com.example.objectdetectionapp.data.firebase

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object PushTokenManager {

    suspend fun getTokenAndSaveToDatabase(uuid: String) {
        try {
            val token = FirebaseMessaging.getInstance().token.await() // from kotlinx-coroutines-play-services
            val dbRef = FirebaseDatabase.getInstance()
                .getReference("fcm_tokens")
                .child(uuid)

            dbRef.setValue(token).await()

            Log.d("PushTokenManager", "✅ FCM Token saved for $uuid")
        } catch (e: Exception) {
            Log.e("PushTokenManager", "❌ Failed to save token: ${e.message}")
        }
    }

}