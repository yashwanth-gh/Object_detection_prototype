package com.example.objectdetectionapp.domain.usecases

import com.example.objectdetectionapp.data.firebase.FirebaseService
import com.example.objectdetectionapp.data.repository.UserPreferencesRepository

class PairOverlookerWithSurveillanceUseCase(
    private val firebaseService: FirebaseService,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    suspend fun execute(surveillanceUUID: String, overlookerUUID: String): Result<Unit> {
        return try {
            if (!firebaseService.isValidSurveillanceUUID(surveillanceUUID)) {
                return Result.failure(Exception("Invalid Surveillance UUID"))
            }

            // Save Surveillance UUID in DataStore
            //userPreferencesRepository.saveUserMode("overlooker", overlookerUUID)
            // Save Surveillance UUID separately
            userPreferencesRepository.saveSurveillanceUUIDToDatastore(surveillanceUUID)

            // Link this Overlooker to Surveillance device
            firebaseService.addOverlookerToSurveillance(surveillanceUUID, overlookerUUID)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
