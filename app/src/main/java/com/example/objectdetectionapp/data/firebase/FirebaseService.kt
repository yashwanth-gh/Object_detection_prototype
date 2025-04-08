package com.example.objectdetectionapp.data.firebase

interface FirebaseService {
    suspend fun saveSurveillanceDevice(uuid: String)
}