package com.example.objectdetectionapp.ui.shared

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.objectdetectionapp.data.models.UserSessionData
import com.example.objectdetectionapp.data.repository.MainRepository
import com.example.objectdetectionapp.data.repository.SignInRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

class ModeSelectionViewModel(
    private val repository: MainRepository,
    private val signInRepository: SignInRepository
) : ViewModel() {

    sealed class ModeSelectionState {
        data object Idle : ModeSelectionState()
        data object Loading : ModeSelectionState()
        data object Success : ModeSelectionState()
        data class Error(val message: String) : ModeSelectionState()
    }

    private val _modeSelectionState = MutableStateFlow<ModeSelectionState>(ModeSelectionState.Idle)
    val modeSelectionState: StateFlow<ModeSelectionState> = _modeSelectionState.asStateFlow()

    private val _userSession = MutableStateFlow(UserSessionData(null, null, null))
    val userSession: StateFlow<UserSessionData> = _userSession.asStateFlow()

    private val _tag = "ModeSelectionVM"

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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
                _isLoading.value = false
                Log.d(_tag, "UserSession loaded: $session")
            }
        }
    }


    fun setMode(mode: String, uuid: String) {
        viewModelScope.launch {
            _modeSelectionState.value = ModeSelectionState.Loading
            try {
                val user = signInRepository.getUserDetails()
                if (user == null) {
                    _modeSelectionState.value = ModeSelectionState.Error("User not signed in.")
                    return@launch
                }

                if (mode == "surveillance") {
                    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                        try {
                            withContext(kotlinx.coroutines.NonCancellable) {
                                repository.saveModeAndUUIDToFirebase(mode, uuid, user)
                            }
                            _modeSelectionState.value = ModeSelectionState.Success
                            Log.d(_tag, "Surveillance data saved to Firebase")
                        } catch (e: Exception) {
                            _modeSelectionState.value = ModeSelectionState.Error(
                                e.localizedMessage ?: "Failed to save mode."
                            )
                            Log.e(_tag, "Failed to save surveillance data: $e")
                        }
                    }
                } else if (mode == "overlooker") {
                    kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                        try {
                            withContext(kotlinx.coroutines.NonCancellable) {
                                repository.saveUserModeAndUUIDToDatastore(mode, uuid)
                            }
                            _modeSelectionState.value = ModeSelectionState.Success
                            Log.d(_tag, "Overlooker mode saved locally")
                        } catch (e: Exception) {
                            _modeSelectionState.value = ModeSelectionState.Error(
                                e.localizedMessage ?: "Failed to save mode."
                            )
                            Log.e(_tag, "Failed to save overlooker mode: $e")
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