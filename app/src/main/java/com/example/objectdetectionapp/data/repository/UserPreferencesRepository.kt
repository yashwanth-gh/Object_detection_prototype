package com.example.objectdetectionapp.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.objectdetectionapp.data.firebase.FirebaseService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(
    private val context: Context,
    private val firebaseService: FirebaseService
) {


    companion object {
        private val MODE_KEY = stringPreferencesKey("mode")  // "surveillance" or "overlooker"
        private val UUID_KEY = stringPreferencesKey("uuid")  // Stores UUID if mode = surveillance
        private const val TAG = "UserPrefsRepo" // Add a TAG for Log messages
    }

    // Save mode and UUID
    suspend fun saveUserMode(mode: String, uuid: String? = null) {
        Log.d(TAG, "Saving user mode: mode=$mode, uuid=$uuid")
        context.dataStore.edit { preferences ->
            preferences[MODE_KEY] = mode
            if (uuid != null) {
                preferences[UUID_KEY] = uuid
            }
        }
    }

    // Get saved mode
    val userMode: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[MODE_KEY]
    }

    // Get saved UUID
    val userUUID: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[UUID_KEY]
    }

    suspend fun getSavedUserModeAndUUID(): Pair<String?, String?> {
        var mode: String? = null
        var uuid: String? = null

        context.dataStore.data.collect { preferences ->
            mode = preferences[MODE_KEY]
            uuid = preferences[UUID_KEY]
            // Break the flow after first value to avoid collecting forever
        }

        // In case no data was emitted (very rare), still return something
        return Pair(mode, uuid)
    }

    suspend fun saveModeWithFirebase(mode: String, uuid: String) {
        firebaseService.saveSurveillanceDevice(uuid)
        saveUserMode(mode, uuid)
    }

    suspend fun saveSurveillanceUUID(surveillanceUUID: String) {
        context.dataStore.edit { preferences ->
            preferences[stringPreferencesKey("surveillance_uuid")] = surveillanceUUID
        }
    }

}