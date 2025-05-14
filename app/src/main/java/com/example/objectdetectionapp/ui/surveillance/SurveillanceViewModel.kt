package com.example.objectdetectionapp.ui.surveillance

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.objectdetectionapp.data.models.BoundingBox
import com.example.objectdetectionapp.data.models.Detection
import com.example.objectdetectionapp.data.models.SurveillanceDevice
import com.example.objectdetectionapp.data.repository.NotificationRepository
import com.example.objectdetectionapp.data.repository.MainRepository
import com.example.objectdetectionapp.domain.usecases.SaveDetectionUseCase
import com.example.objectdetectionapp.tflite.EfficientDetLiteDetector
import com.example.objectdetectionapp.utils.Resource
import com.example.objectdetectionapp.utils.SoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit

class SurveillanceViewModel(
    private val repository: MainRepository,
    private val notificationRepository: NotificationRepository,
    private val saveDetectionUseCase: SaveDetectionUseCase,
    private val soundManager: SoundManager
) : ViewModel() {

    private var _surveillanceUUID = MutableStateFlow("")
    var surveillanceUUID: StateFlow<String?> = _surveillanceUUID.asStateFlow()

    private var lastNotificationTime: Long = 0L
    private var notificationCoolDownDurationMillis = TimeUnit.MINUTES.toMillis(3)

    private var lastDetectionSaveTime: Long = 0L
    private var detectionSaveCoolDownDurationMillis =
        TimeUnit.MINUTES.toMillis(3) // Save every 10 minutes

    private var lastPersonSoundTime: Long = 0L
    private var personSoundCoolDownDurationMillis = TimeUnit.SECONDS.toMillis(10)

    private var lastEmailSentTime: Long = 0L
    private var emailCoolDownMillis = TimeUnit.MINUTES.toMillis(3)

    private val _deviceData = MutableStateFlow<Resource<SurveillanceDevice>>(Resource.Loading())
    val deviceData: StateFlow<Resource<SurveillanceDevice>> = _deviceData.asStateFlow()

    init {
        // Collect UUID in one coroutine
        viewModelScope.launch {
            repository.userUUID.collect { uuid ->
                if (uuid != null) {
                    _surveillanceUUID.value = uuid
                    getSurveillanceDeviceData(uuid)
                }
                Log.d("SurveillanceVM", "UUID Collected in Init: $uuid")
            }
        }

        // Collect settings in a separate coroutine
        viewModelScope.launch {
            Log.d("SurveillanceVM", "⏳ Attempting to collect surveillance settings...")
            repository.getSurveillanceSettings().collect { settings ->
                notificationCoolDownDurationMillis = settings.notificationInterval
                detectionSaveCoolDownDurationMillis = settings.saveInterval
                personSoundCoolDownDurationMillis = settings.soundInterval
                emailCoolDownMillis = settings.emailInterval
                Log.d(
                    "SurveillanceVM",
                    "⏱ Settings Loaded - Notify: ${settings.notificationInterval}, Save: ${settings.saveInterval}, Sound: ${settings.soundInterval}, Email: ${settings.emailInterval}"
                )
            }
        }

    }

    private fun getSurveillanceDeviceData(uuid: String) {
        viewModelScope.launch {
            _deviceData.value = Resource.Loading() // Set loading state
            val result = repository.fetchSurveillanceDevice(uuid)
            _deviceData.value = result // Update with the result (Success or Error)
        }
    }

    fun handleDetectionResults(
        results: List<EfficientDetLiteDetector.DetectionResult>,
        currentFrame: Bitmap?
    ) {
        // Check early if we have any results before filtering
        if (results.isEmpty()) return

        val personDetections = results.filter { it.label.lowercase() == "person" }

        // Avoid further processing if no person detections
        if (personDetections.isEmpty()) return

        val currentTime = System.currentTimeMillis()

        if (currentTime - lastPersonSoundTime >= personSoundCoolDownDurationMillis) {
            lastPersonSoundTime = currentTime
            soundManager.playPersonDetectedSound()
        }

        // Handle both cool down checks in parallel instead of sequentially
        if (currentTime - lastNotificationTime >= notificationCoolDownDurationMillis) {
            lastNotificationTime = currentTime
            notifyOverlookers(
                title = "Person Detected!",
                body = "${personDetections.size} person(s) detected by the surveillance device."
            )
        }

        if (currentTime - lastDetectionSaveTime >= detectionSaveCoolDownDurationMillis) {
            lastDetectionSaveTime = currentTime
            saveDetection(personDetections, currentFrame)
        }

        if (currentTime - lastEmailSentTime >= emailCoolDownMillis) {
            Log.w("SurveillanceVM", "Attempting to send email calling notification repo")
            lastEmailSentTime = currentTime
            sendEmailReport(personDetections, currentFrame)
        }
    }

    private fun saveDetection(
        detections: List<EfficientDetLiteDetector.DetectionResult>,
        currentFrame: Bitmap?,
        uuid: String? = surveillanceUUID.value
    ) {
        viewModelScope.launch {
            detections.forEach { result ->
                launch {
                    val boundingBox = BoundingBox(
                        x = (result.boundingBox.left).toInt(),
                        y = (result.boundingBox.top).toInt(),
                        width = (result.boundingBox.right - result.boundingBox.left).toInt(),
                        height = (result.boundingBox.bottom - result.boundingBox.top).toInt()
                    )
                    val detection = Detection(
                        timestamp = Date().time,
                        label = result.label,
                        confidence = result.confidence,
                        imagePath = null,
                        boundingBox = boundingBox
                    )

                    if (uuid != null) {
                        saveDetectionUseCase.execute(
                            detection,
                            currentFrame,
                            surveillanceUUID = uuid
                        ).collect { resource ->
                            when (resource) {
                                is Resource.Success -> {
                                    Log.d(
                                        "SurveillanceVM",
                                        "Detection saved with ID: ${resource.data}"
                                    )
                                }

                                is Resource.Error -> {
                                    Log.e(
                                        "SurveillanceVM",
                                        "Save error: ${resource.throwable.message}"
                                    )
                                }

                                is Resource.Loading -> {
                                    Log.d("SurveillanceVM", "Saving detection...")
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    private fun notifyOverlookers(
        uuid: String? = surveillanceUUID.value,
        title: String = "Alert!",
        body: String = "Hi you are connected!"
    ) {
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

    private fun sendEmailReport(
        personDetections: List<EfficientDetLiteDetector.DetectionResult>,
        currentFrame: Bitmap?
    ) {
        val resource = deviceData.value
        if (resource is Resource.Success) {
            val deviceData = resource.data
            viewModelScope.launch {
                notificationRepository.sendEmailReportToOverlookers(
                    deviceData,
                    personDetections,
                    image = currentFrame
                )
            }
        }
    }

}

