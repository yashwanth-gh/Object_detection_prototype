package com.example.objectdetectionapp.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.objectdetectionapp.data.firebase.FirebaseService
import com.example.objectdetectionapp.data.models.Overlooker
import com.example.objectdetectionapp.data.models.SurveillanceDevice
import com.example.objectdetectionapp.data.models.SurveillanceSettings
import com.example.objectdetectionapp.data.models.User
import com.example.objectdetectionapp.utils.Resource
import com.example.objectdetectionapp.utils.retryOperation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class MainRepository(
    private val context: Context,
    private val firebaseService: FirebaseService
) {


    companion object {
        private val MODE_KEY = stringPreferencesKey("mode")  // "surveillance" or "overlooker"
        private val UUID_KEY = stringPreferencesKey("uuid")  // Stores UUID if mode = surveillance
        private val NOTIFICATION_INTERVAL_KEY = stringPreferencesKey("notification_interval") // ms
        private val SAVE_INTERVAL_KEY = stringPreferencesKey("save_interval") // ms
        private val SOUND_INTERVAL_KEY = stringPreferencesKey("sound_interval") // ms
        private val EMAIL_INTERVAL_KEY = stringPreferencesKey("email_interval")
        private val DEFAULT_NOTIFICATION_INTERVAL = TimeUnit.MINUTES.toMillis(3)
        private val DEFAULT_SAVE_INTERVAL = TimeUnit.MINUTES.toMillis(3)
        private val DEFAULT_SOUND_INTERVAL = TimeUnit.SECONDS.toMillis(10)
        private val DEFAULT_EMAIL_INTERVAL = TimeUnit.MINUTES.toMillis(3)
        private const val TAG = "UserPrefsRepo"
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

    suspend fun saveSurveillanceSettings(
        notificationIntervalMs: Long,
        saveIntervalMs: Long,
        soundIntervalMs: Long,
        emailIntervalMs: Long
    ) {
        context.dataStore.edit { prefs ->
            prefs[NOTIFICATION_INTERVAL_KEY] = notificationIntervalMs.toString()
            prefs[SAVE_INTERVAL_KEY] = saveIntervalMs.toString()
            prefs[SOUND_INTERVAL_KEY] = soundIntervalMs.toString()
            prefs[EMAIL_INTERVAL_KEY] = emailIntervalMs.toString()
        }
    }


    // Load Settings
    fun getSurveillanceSettings(): Flow<SurveillanceSettings> {
        return context.dataStore.data.map { prefs ->
            SurveillanceSettings(
                notificationInterval = prefs[NOTIFICATION_INTERVAL_KEY]?.toLongOrNull() ?: DEFAULT_NOTIFICATION_INTERVAL,
                saveInterval = prefs[SAVE_INTERVAL_KEY]?.toLongOrNull() ?: DEFAULT_SAVE_INTERVAL,
                soundInterval = prefs[SOUND_INTERVAL_KEY]?.toLongOrNull() ?: DEFAULT_SOUND_INTERVAL,
                emailInterval = prefs[EMAIL_INTERVAL_KEY]?.toLongOrNull() ?: DEFAULT_EMAIL_INTERVAL
            )
        }
    }


}