package com.example.objectdetectionapp.ui.overlooker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.objectdetectionapp.data.firebase.FirebaseService
import com.example.objectdetectionapp.data.repository.UserPreferencesRepository
import com.example.objectdetectionapp.domain.usecases.PairOverlookerWithSurveillanceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OverlookerPairViewModel(
    private val overlookerUUID: String,
    private val pairUseCase: PairOverlookerWithSurveillanceUseCase
) : ViewModel() {

    sealed class PairingState {
        object Idle : PairingState()
        object Loading : PairingState()
        object Success : PairingState()
        data class Error(val message: String) : PairingState()
    }

    private val _pairingState = MutableStateFlow<PairingState>(PairingState.Idle)
    val pairingState: StateFlow<PairingState> = _pairingState

    fun pairWithSurveillanceDevice(surveillanceUUID: String) {
        viewModelScope.launch {
            _pairingState.value = PairingState.Loading

            val result = pairUseCase.execute(surveillanceUUID, overlookerUUID)

            _pairingState.value = if (result.isSuccess) {
                PairingState.Success
            } else {
                PairingState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}