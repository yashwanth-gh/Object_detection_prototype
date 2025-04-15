package com.example.objectdetectionapp.ui.overlooker

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.objectdetectionapp.data.firebase.FCMService
import com.example.objectdetectionapp.data.firebase.FirebaseService
import com.example.objectdetectionapp.data.repository.UserPreferencesRepository
import com.example.objectdetectionapp.domain.usecases.PairOverlookerWithSurveillanceUseCase
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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

    fun sendNotificationToSurveillance(
        context: Context,
        surveillanceUUID: String,
        overlookerUUID: String
    ) {
        viewModelScope.launch {
            try {
                Log.d("OverlookerVM", "Attempting to send notification to $surveillanceUUID")

                val fcmService = FCMService(context)

                val tokenSnap = FirebaseDatabase.getInstance()
                    .getReference("fcm_tokens/$surveillanceUUID")
                    .get().await()

                val fcmToken = tokenSnap.getValue(String::class.java)

                if (!fcmToken.isNullOrEmpty()) {
                    Log.d("OverlookerVM", "FCM token found: $fcmToken")

                    fcmService.sendNotificationToToken(
                        token = fcmToken,
                        title = "You are Paired! 🔗",
                        body = "Hi, you are connected to:\nUUID: $overlookerUUID"
                    )

                    Log.d("OverlookerVM", "Notification sent successfully.")

                } else {
                    Log.w("OverlookerVM", "FCM token is null or empty for UUID: $surveillanceUUID")
                }

            } catch (e: Exception) {
                Log.e("OverlookerVM", "Error sending notification: ${e.message}", e)
            }
        }
    }
}