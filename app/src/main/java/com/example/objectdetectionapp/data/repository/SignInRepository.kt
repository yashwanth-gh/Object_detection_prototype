package com.example.objectdetectionapp.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.objectdetectionapp.data.firebase.FirebaseService
import com.example.objectdetectionapp.data.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

class SignInRepository(
    appContext: Context,
    private val firebaseService: FirebaseService
) {
    private val TAG = "SignInRepository"
    private val dataStore = appContext.userDataStore
    private val usernameKey = stringPreferencesKey("username")
    private val emailKey = stringPreferencesKey("email")
    private val hasSignedInKey = stringPreferencesKey("has_signed_in")

    val user: Flow<User> = dataStore.data.map { preferences ->
        User(
            username = preferences[usernameKey] ?: "",
            email = preferences[emailKey] ?: ""
        )
    }

    val hasSignedIn: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[hasSignedInKey] != null
    }

    /**
     * Gets the current user details from DataStore
     * @return User object if the user has signed in, null otherwise
     */
    suspend fun getUserDetails(): User? {
        try {
            val preferences = dataStore.data.first()
            val username = preferences[usernameKey]
            val email = preferences[emailKey]

            return if (username != null && email != null) {
                Log.d(TAG, "Retrieved user details: username=$username, email=$email")
                User(username, email)
            } else {
                Log.d(TAG, "No user details found in DataStore")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving user details: ${e.message}")
            return null
        }
    }



    private suspend fun saveUserToDataStore(user: User) {
        dataStore.edit { preferences ->
            preferences[usernameKey] = user.username
            preferences[emailKey] = user.email
            preferences[hasSignedInKey] = "true" // Mark as signed in
        }
        Log.d(TAG, "User saved to DataStore: ${user.username}")
    }

    suspend fun saveUserDetails(user: User) {
        saveUserToDataStore(user)
        firebaseService.saveUser(user)
        Log.d(TAG, "User details saved locally and to Firebase: ${user.username}")
    }

    suspend fun clearUserSession() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
        Log.d(TAG, "User session cleared")
    }

    val userDataFlow: Flow<User> = dataStore.data
        .map { preferences ->
            val username = preferences[usernameKey] ?: ""
            val email = preferences[emailKey] ?: ""
            User(username = username, email = email)
        }

}