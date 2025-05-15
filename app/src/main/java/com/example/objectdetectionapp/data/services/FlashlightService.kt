package com.example.objectdetectionapp.data.services

interface FlashlightService {
    fun turnOnFlashlight()
    fun turnOffFlashlight()
    fun isFlashAvailable(): Boolean
}