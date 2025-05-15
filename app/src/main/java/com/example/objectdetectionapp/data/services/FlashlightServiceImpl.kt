package com.example.objectdetectionapp.data.services

import android.content.Context
import android.hardware.camera2.CameraManager
import android.util.Log

class FlashlightServiceImpl(private val context: Context) : FlashlightService {

    private var cameraId: String? = null
    private val cameraManager: CameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    init {
        try {
            cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) {
            Log.e("FlashlightService", "Error initializing camera ID: ${e.message}")
        }
    }

    override fun turnOnFlashlight() {
        try {
            cameraId?.let {
                cameraManager.setTorchMode(it, true)
                Log.d("FlashlightService", "🔦 Flashlight ON")
            }
        } catch (e: Exception) {
            Log.e("FlashlightService", "Error turning ON flashlight: ${e.message}")
        }
    }

    override fun turnOffFlashlight() {
        try {
            cameraId?.let {
                cameraManager.setTorchMode(it, false)
                Log.d("FlashlightService", "🔦 Flashlight OFF")
            }
        } catch (e: Exception) {
            Log.e("FlashlightService", "Error turning OFF flashlight: ${e.message}")
        }
    }

    override fun isFlashAvailable(): Boolean {
        return cameraId != null
    }
}