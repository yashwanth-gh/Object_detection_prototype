package com.example.objectdetectionapp.data.firebase

interface FirebaseService {
    suspend fun saveSurveillanceDevice(uuid: String)
    suspend fun isValidSurveillanceUUID(uuid: String): Boolean
    suspend fun addOverlookerToSurveillance(surveillanceUUID: String, overlookerUUID: String)
    suspend fun getTokenAndSaveToDatabase(uuid: String)
    suspend fun getOverlookerFCMTokens(surveillanceUUID: String): List<String>
    suspend fun getSurveillanceFCMToken(surveillanceUUID: String): String?
    suspend fun fetchFullSurveillanceUUID(pairingCode: String): String?
    suspend fun checkIfFCMTokenExists(uuid: String): Boolean
}