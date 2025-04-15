package com.example.objectdetectionapp.ui.surveillance

import android.content.Context
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.objectdetectionapp.data.firebase.FCMService
import com.example.objectdetectionapp.data.repository.UserPreferencesRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SurveillanceViewModel(
    private val repository: UserPreferencesRepository
) : ViewModel() {

    var savedMode: String? = null
        private set

    var savedUUID: String? = null
        private set

    init {
        viewModelScope.launch {
            val (mode, uuid) = repository.getSavedUserModeAndUUID()
            savedMode = mode
            savedUUID = uuid
            Log.d("SurveillanceVM", "Mode: $mode, UUID: $uuid")
        }
    }

    fun generateUniqueId(): String {
        return UUID.randomUUID().toString()
    }

    fun saveUniqueIdToFirebase(uniqueId: String) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("surveillance_devices").child(uniqueId).setValue(mapOf("status" to "active"))
        Log.d("Firebase", "ID saved successfully")
    }

    fun sendNotificationToOverlookers(context: Context, surveillanceUUID: String) {
        viewModelScope.launch {
            try {
                val fcmService = FCMService(context)

                val dbRef = FirebaseDatabase.getInstance()
                    .getReference("surveillance_devices/$surveillanceUUID/overlookers")

                val snapshot = dbRef.get().await()

                for (overlooker in snapshot.children) {
                    val overlookerUUID = overlooker.key ?: continue

                    val tokenSnap = FirebaseDatabase.getInstance()
                        .getReference("fcm_tokens/$overlookerUUID")
                        .get().await()

                    val fcmToken = tokenSnap.getValue(String::class.java)
                    if (!fcmToken.isNullOrEmpty()) {
                        fcmService.sendNotificationToToken(
                            token = fcmToken,
                            title = "Alert from Surveillance",
                            body = "Hi you are connected to :\nUUID: $surveillanceUUID"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("SurveillanceVM", "Error sending notifications: ${e.message}", e)
                // Handle the error appropriately, perhaps show a message to the user.
            }
        }
    }


}

