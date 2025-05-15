package com.example.objectdetectionapp.data.models

data class SurveillanceDevice(
    val pairingCode: String = "",
    val status: String = "",
    val user: User = User(),
    val overlookers: Map<String, Overlooker> = emptyMap(),
    val detections: Map<String, Detection>? = null ,
    val nightMode: Boolean = false ,
    val startCamera :Boolean = false
    )

data class Overlooker(
    val email: String = "",
    val username: String = "",
    val uuid: String = ""
)
