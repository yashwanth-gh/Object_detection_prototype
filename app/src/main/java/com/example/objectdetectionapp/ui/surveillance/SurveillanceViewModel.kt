package com.example.objectdetectionapp.ui.surveillance

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.objectdetectionapp.data.firebase.FCMService
import com.example.objectdetectionapp.data.repository.NotificationRepository
import com.example.objectdetectionapp.data.repository.UserPreferencesRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SurveillanceViewModel(
    private val repository: UserPreferencesRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    var savedMode: String? = null
        private set

    var savedUUID: String? = null
        private set

    init {
        viewModelScope.launch {
            val (mode, uuid) = repository.getSavedUserModeAndUUIDFromDatastore()
            savedMode = mode
            savedUUID = uuid
            Log.d("SurveillanceVM", "Mode: $mode, UUID: $uuid")
        }
    }


    fun notifyOverlookers(surveillanceUUID: String) {
        viewModelScope.launch {
            try {
                notificationRepository.sendNotificationToOverlookers(surveillanceUUID)
            } catch (e: Exception) {
                Log.e("SurveillanceVM", "Error sending notifications: ${e.message}")
            }
        }
    }

}

