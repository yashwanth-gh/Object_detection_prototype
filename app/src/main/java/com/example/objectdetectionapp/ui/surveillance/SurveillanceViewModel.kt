package com.example.objectdetectionapp.ui.surveillance

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.objectdetectionapp.data.repository.NotificationRepository
import com.example.objectdetectionapp.data.repository.MainRepository
import com.example.objectdetectionapp.tflite.EfficientDetLiteDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SurveillanceViewModel(
    private val repository: MainRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private var _surveillanceUUID = MutableStateFlow("")
    var surveillanceUUID: StateFlow<String?> = _surveillanceUUID.asStateFlow()

    private var lastNotificationTime: Long = 0L

    private val coolDownDurationMillis = TimeUnit.MINUTES.toMillis(3) // Example cool down

    init {
        viewModelScope.launch {
            repository.userUUID.collect { uuid ->
                if (uuid != null) {
                    _surveillanceUUID.value = uuid
                }
                Log.d("SurveillanceVM", "UUID: $uuid")
            }
        }
    }

    fun handleDetectionResults(results: List<EfficientDetLiteDetector.DetectionResult>) {
        val personDetected = results.any { it.label.lowercase() == "person" }
        val currentTime = System.currentTimeMillis()

        if (personDetected && (currentTime - lastNotificationTime >= coolDownDurationMillis)) {
            lastNotificationTime = currentTime
            notifyOverlookers(
                title = "Person Detected!",
                body = "A person has been detected by the surveillance device."
            )
        }
    }


    fun notifyOverlookers(uuid: String? = surveillanceUUID.value, title:String="Alert!", body:String = "Hi you are connected!") {
        viewModelScope.launch {
            try {
                if (uuid != null) {
                    notificationRepository.sendNotificationToOverlookers(
                        surveillanceUUID = uuid,
                        title,
                        body
                    )
                }
                    Log.w(
                        "SurveillanceVM",
                        "notificationRepository.sendNotificationToOverlookers() is called"
                    )

            } catch (e: Exception) {
                Log.e("SurveillanceVM", "Error sending notifications: ${e.message}")
            }
        }
    }

}

