package com.example.objectdetectionapp.ui.surveillance

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.objectdetectionapp.data.repository.NotificationRepository
import com.example.objectdetectionapp.data.repository.UserPreferencesRepository
import kotlinx.coroutines.launch

class SurveillanceViewModel(
    private val repository: UserPreferencesRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private var savedMode: String? = null

    private var savedUUID: String? = null

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

