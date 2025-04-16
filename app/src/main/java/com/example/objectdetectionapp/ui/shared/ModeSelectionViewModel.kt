package com.example.objectdetectionapp.ui.shared

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.objectdetectionapp.data.models.UserSessionData
import com.example.objectdetectionapp.data.repository.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class ModeSelectionViewModel(private val repository: UserPreferencesRepository) : ViewModel() {

    private val _userSession = MutableStateFlow(UserSessionData(null, null, null))
    val userSession: StateFlow<UserSessionData> = _userSession.asStateFlow()

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
                    Log.d(_tag, "Launching independent job for Firebase save")

                    // Launch a separate coroutine that's not cancelled with ViewModel
                    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                        try {
                            withContext(NonCancellable) {
                                repository.saveModeAndUUIDToFirebase(mode, uuid)
                                Log.d(_tag, "Surveillance data saved to Firebase")
                            }
                        } catch (e: Exception) {
                            Log.e(_tag, "Failed inside independent job: $e")
                        }
                    }

                } else if (mode == "overlooker") {
                    // Launch a separate coroutine that's not cancelled with ViewModel
                    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                        try {
                            withContext(NonCancellable) {
                                repository.saveUserModeAndUUIDToDatastore(mode, uuid)
                                Log.d(_tag, "Overlooker mode saved locally")
                            }
                        } catch (e: Exception) {
                            Log.e(_tag, "Failed inside independent job: $e")
                        }
                    }
                }

            } catch (e: CancellationException) {
                Log.w(_tag, "ViewModel coroutine was cancelled")
            } catch (e: Exception) {
                Log.e(_tag, "Outer catch: $e")
            }
        }
    }

}