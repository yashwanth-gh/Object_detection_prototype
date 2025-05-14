package com.example.objectdetectionapp.data.models

data class SurveillanceSettings(
    val notificationInterval: Long,
    val saveInterval: Long,
    val soundInterval: Long,
    val emailInterval: Long
)
