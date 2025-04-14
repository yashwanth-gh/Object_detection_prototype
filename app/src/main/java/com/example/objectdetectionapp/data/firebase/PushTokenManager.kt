package com.example.objectdetectionapp.data.firebase

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

object PushTokenManager {

    fun saveTokenToDatabase(uuid: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("PushTokenManager", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            val dbRef = FirebaseDatabase.getInstance()
                .getReference("fcm_tokens")
                .child(uuid)

            dbRef.setValue(token)
                .addOnSuccessListener {
                    Log.d("PushTokenManager", "✅ FCM Token saved for $uuid")
                }
                .addOnFailureListener {
                    Log.e("PushTokenManager", "❌ Failed to save token: ${it.message}")
                }
        }
    }
}