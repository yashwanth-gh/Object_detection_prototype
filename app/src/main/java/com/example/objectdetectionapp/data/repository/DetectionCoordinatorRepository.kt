package com.example.objectdetectionapp.data.repository

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraControl
import com.example.objectdetectionapp.data.firebase.FirebaseService
import com.example.objectdetectionapp.data.services.FlashlightService
import com.example.objectdetectionapp.data.services.SoundDetector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DetectionCoordinatorRepository(
    private val flashlightService: FlashlightService,
    private val soundDetector: SoundDetector,
    private val firebaseService: FirebaseService
) {
    private var cameraControl: CameraControl? = null

    fun setCameraControl(control: CameraControl) {
        Log.d("DetectionCoordinator", "CameraControl has been set.")
        cameraControl = control
    }

    fun startSoundDetectionAndTriggerFlash(context: Context) {
        Log.d("DetectionCoordinator", "Starting sound detection.")
        soundDetector.startListening(context) {
            Log.d("DetectionCoordinator", "Sound detected! Triggering flashlight.")

            if (cameraControl == null) {
                Log.w("DetectionCoordinator", "CameraControl is null, cannot enable torch.")
            } else {
                cameraControl?.enableTorch(true)
                Log.d("DetectionCoordinator", "Torch enabled.")

                CoroutineScope(Dispatchers.Default).launch {
                    Log.d("DetectionCoordinator", "Torch will remain on for 10 seconds.")
                    delay(30_000)
                    cameraControl?.enableTorch(false)
                    Log.d("DetectionCoordinator", "Torch disabled after delay.")
                }
            }
        }
    }

    fun stopSoundDetection() {
        Log.d("DetectionCoordinator", "Stopping sound detection.")
        soundDetector.stopListening()
    }

    suspend fun observeNightMode(uuid: String): Flow<Boolean> {
        Log.d("DetectionCoordinator", "Observing night mode for UUID: $uuid")
        return firebaseService.observeNightMode(uuid)
    }
}
