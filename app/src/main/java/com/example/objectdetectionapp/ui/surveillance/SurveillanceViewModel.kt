package com.example.objectdetectionapp.ui.surveillance

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraControl
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.objectdetectionapp.data.models.BoundingBox
import com.example.objectdetectionapp.data.models.Detection
import com.example.objectdetectionapp.data.models.SurveillanceDevice
import com.example.objectdetectionapp.data.repository.DetectionCoordinatorRepository
import com.example.objectdetectionapp.data.repository.NotificationRepository
import com.example.objectdetectionapp.data.repository.MainRepository
import com.example.objectdetectionapp.domain.usecases.SaveDetectionUseCase
import com.example.objectdetectionapp.tflite.EfficientDetLiteDetector
import com.example.objectdetectionapp.utils.Resource
import com.example.objectdetectionapp.utils.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.concurrent.TimeUnit

class SurveillanceViewModel(
    private val context: Context,
    private val repository: MainRepository,
    private val notificationRepository: NotificationRepository,
    private val saveDetectionUseCase: SaveDetectionUseCase,
    private val soundManager: SoundManager,
    private val detectionCoordinatorRepository: DetectionCoordinatorRepository
) : ViewModel() {

    private var _surveillanceUUID = MutableStateFlow("")
    var surveillanceUUID: StateFlow<String?> = _surveillanceUUID.asStateFlow()

    private var _nightMode = MutableStateFlow(false)
    var nightMode: StateFlow<Boolean> = _nightMode.asStateFlow()

    private var _goToCameraPreviewScreen = MutableStateFlow(false)
    var goToCameraPreviewScreen: StateFlow<Boolean> = _goToCameraPreviewScreen.asStateFlow()

    private var _goToSurveillanceHome = MutableStateFlow(false)
    var goToSurveillanceHome: StateFlow<Boolean> = _goToSurveillanceHome.asStateFlow()

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

    private var cameraControl: CameraControl? = null

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
            if (result is Resource.Success) {
                _nightMode.value = result.data.nightMode
            }
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

    fun setCameraControl(control: CameraControl) {
        cameraControl = control
        detectionCoordinatorRepository.setCameraControl(control)
    }

    fun observeNightMode(uuid: String? = surveillanceUUID.value) {
        viewModelScope.launch {
            Log.w("SurveillanceVM", "NightMode for uuid: $uuid")
            if (uuid != null) {
                Log.w("SurveillanceVM", "calling detection repo")
                detectionCoordinatorRepository.observeNightMode(uuid).collect { nightModeOn ->
                    Log.w("SurveillanceVM", "Night Mode is $nightModeOn")
                    if (nightModeOn) {
                        detectionCoordinatorRepository.startSoundDetectionAndTriggerFlash(context)
                    } else {
                        detectionCoordinatorRepository.stopSoundDetection()
                    }
                }
            }
        }
    }

    private var nightModeJob: Job? = null

    fun startObservingNightMode(uuid: String) {
        nightModeJob?.cancel()
        nightModeJob = viewModelScope.launch {
            detectionCoordinatorRepository.observeNightMode(uuid).collect { nightModeOn ->
                if (nightModeOn) {
                    detectionCoordinatorRepository.startSoundDetectionAndTriggerFlash(context)
                } else {
                    detectionCoordinatorRepository.stopSoundDetection()
                }
            }
        }
    }

    fun stopObservingNightMode() {
        nightModeJob?.cancel()
        nightModeJob = null
        detectionCoordinatorRepository.stopSoundDetection()
    }

    fun setNightMode(uuid: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.updateNightMode(uuid, enabled)
            // the DB will fire back and update `_nightMode` via your observer
            _nightMode.value = enabled
        }
    }

    fun listenForNightModeChangesToUpdateTheSwitch(uuid: String? = surveillanceUUID.value){
        viewModelScope.launch {
            Log.w("SurveillanceVM", "NightMode for uuid: $uuid")
            if (uuid != null) {
                Log.w("SurveillanceVM", "calling detection repo")
                detectionCoordinatorRepository.observeNightMode(uuid).collect { nightModeOn ->
                    Log.w("SurveillanceVM", "Night Mode is $nightModeOn")
                    _nightMode.value = nightModeOn
                }
            }
        }
    }

    fun observeStartCamera(uuid: String? = surveillanceUUID.value) {
        viewModelScope.launch {
            Log.w("SurveillanceVM", "Start Camera for uuid: $uuid")
            if (uuid != null) {
                Log.w("SurveillanceVM", "calling main repo")
                repository.observeStartCamera(uuid).collect { startCameraOn ->
                    Log.w("SurveillanceVM", "Night Mode is $startCameraOn")
                    if (startCameraOn) {
                        _goToCameraPreviewScreen.value = true
                        _goToSurveillanceHome.value = false
                        Log.w("SurveillanceVM", "Navigate to CameraPreviewScreen")
                    } else {
                        _goToSurveillanceHome.value = true
                        _goToCameraPreviewScreen.value = false
                        Log.w("SurveillanceVM", "Navigate to SurveillanceScreen")
                    }
                }
            }
        }
    }


}

