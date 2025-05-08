package com.example.objectdetectionapp.domain.usecases

import android.util.Log
import com.example.objectdetectionapp.data.firebase.FirebaseService
import com.example.objectdetectionapp.data.models.User
import com.example.objectdetectionapp.data.repository.MainRepository
import com.example.objectdetectionapp.data.repository.NotificationRepository

class PairOverlookerWithSurveillanceUseCase(
    private val firebaseService: FirebaseService,
    private val mainRepository: MainRepository,
    private val notificationRepository: NotificationRepository
) {
    private val _tag = "PairingUseCase"

    suspend fun execute(
        surveillanceUUID: String,
        overlookerUUID: String,
        user: User
    ): Result<Unit> {
        Log.d(
            _tag,
            "Attempting to pair Overlooker: $overlookerUUID with Surveillance: $surveillanceUUID"
        )
        return try {
            Log.d(_tag, "Checking if Surveillance UUID ($surveillanceUUID) is valid...")

            if (!firebaseService.isValidSurveillanceUUID(surveillanceUUID)) {
                Log.w(_tag, "Surveillance UUID ($surveillanceUUID) is invalid.")
                return Result.failure(Exception("Invalid Surveillance UUID"))
            }

            Log.d(_tag, "Surveillance UUID ($surveillanceUUID) is valid.")

            Log.d(_tag, "Saving Surveillance UUID ($surveillanceUUID) to DataStore.")

            mainRepository.saveSurveillanceUUIDToDatastore(surveillanceUUID)

            Log.d(_tag, "Surveillance UUID ($surveillanceUUID) saved to DataStore.")

            Log.d(
                _tag,
                "Adding Overlooker ($overlookerUUID) to Surveillance ($surveillanceUUID) in Firebase."
            )
            firebaseService.addOverlookerToSurveillance(surveillanceUUID, overlookerUUID,user)
            Log.d(
                _tag,
                "Overlooker ($overlookerUUID) added to Surveillance ($surveillanceUUID) in Firebase."
            )

            Log.d(_tag, "Checking if FCM token exists for Surveillance Device ($surveillanceUUID).")
            if (!notificationRepository.checkIfFCMTokenAreSavedInDB(surveillanceUUID)) {
                Log.w(
                    _tag,
                    "FCM token not found for Surveillance Device ($surveillanceUUID). Attempting to save using Overlooker UUID ($overlookerUUID)."
                )
                mainRepository.saveFCMTokenToFirebase(overlookerUUID)
                Log.d(
                    _tag,
                    "Attempted to save FCM token for Surveillance Device ($surveillanceUUID) using Overlooker UUID ($overlookerUUID)."
                )
            } else {
                Log.d(_tag, "FCM token found for Surveillance Device ($surveillanceUUID).")
            }

            Log.d(_tag, "Checking if FCM token exists for Overlooker Device ($overlookerUUID).")
            if (!notificationRepository.checkIfFCMTokenAreSavedInDB(overlookerUUID)) {
                Log.w(
                    _tag,
                    "FCM token not found for Overlooker Device ($overlookerUUID). Attempting to save using Surveillance UUID ($surveillanceUUID)."
                )
                mainRepository.saveFCMTokenToFirebase(surveillanceUUID)
                Log.d(
                    _tag,
                    "Attempted to save FCM token for Overlooker Device ($overlookerUUID) using Surveillance UUID ($surveillanceUUID)."
                )
            } else {
                Log.d(_tag, "FCM token found for Overlooker Device ($overlookerUUID).")
            }

            Log.d(
                _tag,
                "Pairing successful between Overlooker: $overlookerUUID and Surveillance: $surveillanceUUID."
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(
                _tag,
                "Error during pairing of Overlooker: $overlookerUUID with Surveillance: $surveillanceUUID",
                e
            )
            Result.failure(e)
        }
    }
}