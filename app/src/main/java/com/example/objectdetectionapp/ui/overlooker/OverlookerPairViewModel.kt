package com.example.objectdetectionapp.ui.overlooker

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.objectdetectionapp.data.repository.NotificationRepository
import com.example.objectdetectionapp.data.repository.UserPreferencesRepository
import com.example.objectdetectionapp.domain.usecases.PairOverlookerWithSurveillanceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OverlookerPairViewModel(
    private val overlookerUUID: String,
    private val pairUseCase: PairOverlookerWithSurveillanceUseCase,
    private val notificationRepository:NotificationRepository,
    private val userPreferencesRepository:UserPreferencesRepository
) : ViewModel() {

    sealed class PairingState {
        data object Idle : PairingState()
        data object Loading : PairingState()
        data object Success : PairingState()
        data class Error(val message: String) : PairingState()
    }

    private val _pairingState = MutableStateFlow<PairingState>(PairingState.Idle)
    val pairingState: StateFlow<PairingState> = _pairingState

    private val _surveillanceUUID = MutableStateFlow("")
    val surveillanceUUID: StateFlow<String> = _surveillanceUUID

    fun pairWithSurveillanceDevice(pairingCode: String) {
        viewModelScope.launch {
            _pairingState.value = PairingState.Loading

            // call and get full uuid from getFullUUIDFromPairingCode make sure it is not null
            // Fetch full UUID using the pairing code
            val fullUUID = userPreferencesRepository.getFullUUIDFromPairingCode(pairingCode)

            if (fullUUID == null) {
                _pairingState.value = PairingState.Error("Invalid pairing code or failed to fetch UUID.")
                return@launch
            }

            val result = pairUseCase.execute(fullUUID, overlookerUUID)

            _pairingState.value = if (result.isSuccess) {
                _surveillanceUUID.value = fullUUID // Store the full UUID for navigation
                PairingState.Success
            } else {
                PairingState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }


    fun notifySurveillanceOfPairing(surveillanceUUID: String) {
        viewModelScope.launch {
            try {
                notificationRepository.sendPairingNotificationToSurveillance(
                    surveillanceUUID,
                    overlookerUUID
                )
            } catch (e: Exception) {
                Log.e("OverlookerVM", "Notification error: ${e.message}", e)
            }
        }
    }
}