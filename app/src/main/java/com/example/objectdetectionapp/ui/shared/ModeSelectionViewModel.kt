package com.example.objectdetectionapp.ui.shared

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.objectdetectionapp.data.firebase.PushTokenManager
import com.example.objectdetectionapp.data.models.UserSessionData
import com.example.objectdetectionapp.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID

class ModeSelectionViewModel(private val repository: UserPreferencesRepository) : ViewModel() {


    /*    private val _mode = MutableStateFlow<String?>(null)
        val mode = _mode.asStateFlow()

        private val _uuid = MutableStateFlow<String?>(null)
        val uuid = _uuid.asStateFlow()*/

    private val _userSession = MutableStateFlow(UserSessionData(null, null, null))
    val userSession: StateFlow<UserSessionData> = _userSession.asStateFlow()


    //    private val firebaseRef = FirebaseDatabase.getInstance().reference
    private val _tag = "ModeSelectionVM"

    init {
        viewModelScope.launch {
            combine(
                repository.userMode,
                repository.userUUID,
                repository.connectedSurveillanceUUID
            ) { mode, uuid, connectedUUID ->
                UserSessionData(mode, uuid, connectedUUID)
            }.collect { session ->
                _userSession.value = session
                Log.d(_tag, "UserSession loaded: $session")
            }
        }
    }

    fun setMode(mode: String, uuid: String) {
        viewModelScope.launch {

            try {
                if (mode == "surveillance") {
                    repository.saveModeWithFirebase(mode, uuid)
                } else {
                    repository.saveUserMode(mode, uuid)
                }
            } catch (e: Exception) {
                Log.e(_tag, "Failed to save to Firebase: ${e.message}")
                return@launch
            }

            saveFCMTokenToDB(uuid)

        }
    }

    fun saveFCMTokenToDB(uuid: String) {
        try {
            Log.d(_tag, "saveFCMTokenToDB triggered")
            PushTokenManager.saveTokenToDatabase(uuid)
        } catch (e: Exception) {
            Log.e(_tag, "Error saving fcm token: ${e.message}")
        }
    }
}