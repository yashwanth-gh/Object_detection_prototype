package com.example.objectdetectionapp.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.objectdetectionapp.data.firebase.FirebaseService
import com.example.objectdetectionapp.data.models.Overlooker
import com.example.objectdetectionapp.data.models.SurveillanceDevice
import com.example.objectdetectionapp.data.models.User
import com.example.objectdetectionapp.utils.Resource
import com.example.objectdetectionapp.utils.retryOperation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class MainRepository(
    private val context: Context,
    private val firebaseService: FirebaseService
) {


    companion object {
        private val MODE_KEY = stringPreferencesKey("mode")  // "surveillance" or "overlooker"
        private val UUID_KEY = stringPreferencesKey("uuid")  // Stores UUID if mode = surveillance
        private const val TAG = "UserPrefsRepo" // Add a TAG for Log messages
    }

    // Get saved mode
    val userMode: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[MODE_KEY]
    }

    // Get saved UUID
    val userUUID: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[UUID_KEY]
    }

    val connectedSurveillanceUUID: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[stringPreferencesKey("surveillance_uuid")]
        }

    // Save mode and UUID
    suspend fun saveUserModeAndUUIDToDatastore(mode: String, uuid: String? = null) {
        Log.d(TAG, "Saving user mode: mode=$mode, uuid=$uuid")
        context.dataStore.edit { preferences ->
            preferences[MODE_KEY] = mode
            if (uuid != null) {
                preferences[UUID_KEY] = uuid
            }
        }

        if (uuid != null) {
            Log.d(TAG, "Calling saveFCMTokenToFirebase for $uuid")
            saveFCMTokenToFirebase(uuid)
        }
    }

    suspend fun saveSurveillanceUUIDToDatastore(surveillanceUUID: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey("surveillance_uuid")] = surveillanceUUID
        }
    }


    suspend fun saveModeAndUUIDToFirebase(
        mode: String,
        uuid: String,
        user: User
    ) {
        retryOperation(
            maxAttempts = 3,
            delayMillis = 1500,
            operationName = "saveSurveillanceDevice"
        ) {
            firebaseService.saveSurveillanceDevice(uuid, user)
        }
        saveUserModeAndUUIDToDatastore(mode, uuid)
    }

    suspend fun saveFCMTokenToFirebase(uuid: String) {
        try {
            Log.d(TAG, "saveFCMTokenToFirebase triggered")

            retryOperation(
                maxAttempts = 3,
                delayMillis = 2500,
                operationName = "SaveFCMToken"
            ) {
                firebaseService.getTokenAndSaveToDatabase(uuid)
            }

            Log.d(TAG, "✅ Token saved after retry logic (if needed)")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving fcm token: ${e.message}")
        }
    }

    suspend fun getFullUUIDFromPairingCode(pairingCode: String): String? {
        return try {
            // Retry fetching the UUID based on the pairing code
            retryOperation(maxAttempts = 3, delayMillis = 1500, operationName = "Get Full UUID") {
                firebaseService.fetchFullSurveillanceUUID(pairingCode)
            }
        } catch (e: Exception) {
            Log.e("UserPrefsRepo", "Error fetching full UUID for pairing code: ${e.message}")
            null
        }
    }

    suspend fun fetchSurveillanceDevice(uuid: String): Resource<SurveillanceDevice> {
        return try {
            val device = firebaseService.getSurveillanceDevice(uuid)
            if (device != null) {
                Resource.Success(device)
            } else {
                Resource.Error(Exception("Device not found"))
            }
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    suspend fun getPairedOverlookers(surveillanceUUID: String): Resource<List<Overlooker>> {
        return try {
            val overlookers = firebaseService.getOverlookersForSurveillance(surveillanceUUID)
            Resource.Success(overlookers)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    suspend fun deleteOverlooker(surveillanceUUID: String, overlookerUUID: String): Resource<Unit> {
        return try {
            firebaseService.deleteOverlookerFromSurveillance(surveillanceUUID, overlookerUUID)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    suspend fun isOverlookerPaired(
        surveillanceUUID: String,
        overlookerUUID: String
    ): Resource<Boolean> {
        return try {
            val overlooker =
                firebaseService.getOverlookerForSurveillanceDevice(surveillanceUUID, overlookerUUID)
            Resource.Success(overlooker != null)
        } catch (e: Exception) {
            Resource.Error(e)
        }
    }

    suspend fun clearUserData() {
        context.dataStore.edit { preferences ->
            preferences.remove(MODE_KEY)
            preferences.remove(UUID_KEY)
            preferences.remove(stringPreferencesKey("surveillance_uuid"))
        }
    }

}