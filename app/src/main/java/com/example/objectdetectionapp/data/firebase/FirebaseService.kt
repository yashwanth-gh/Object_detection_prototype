package com.example.objectdetectionapp.data.firebase

import com.example.objectdetectionapp.data.models.Detection
import com.example.objectdetectionapp.data.models.SurveillanceDevice
import com.example.objectdetectionapp.data.models.User
import kotlinx.coroutines.flow.Flow

interface FirebaseService {
    suspend fun saveSurveillanceDevice(uuid: String, user:User)
    suspend fun isValidSurveillanceUUID(uuid: String): Boolean
    suspend fun addOverlookerToSurveillance(surveillanceUUID: String, overlookerUUID: String,user:User)
    suspend fun getTokenAndSaveToDatabase(uuid: String)
    suspend fun getOverlookerFCMTokens(surveillanceUUID: String): List<String>
    suspend fun getSurveillanceFCMToken(surveillanceUUID: String): String?
    suspend fun fetchFullSurveillanceUUID(pairingCode: String): String?
    suspend fun checkIfFCMTokenExists(uuid: String): Boolean
    suspend fun fetchDetectionDetails(surveillanceUUID: String): Flow<List<Detection>>
    suspend fun saveUser(user: User)
    suspend fun getSurveillanceDevice(uuid: String): SurveillanceDevice?

}