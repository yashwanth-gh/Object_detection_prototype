package com.example.objectdetectionapp.data.firebase

interface FirebaseService {
    suspend fun saveSurveillanceDevice(uuid: String)
    suspend fun isValidSurveillanceUUID(uuid: String): Boolean
    suspend fun addOverlookerToSurveillance(surveillanceUUID: String, overlookerUUID: String)
}