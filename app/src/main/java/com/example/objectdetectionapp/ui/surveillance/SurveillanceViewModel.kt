package com.example.objectdetectionapp.ui.surveillance

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.objectdetectionapp.data.repository.UserPreferencesRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import java.util.UUID

class SurveillanceViewModel(
    private val repository: UserPreferencesRepository
) : ViewModel() {

    var savedMode: String? = null
        private set

    var savedUUID: String? = null
        private set

    init {
        viewModelScope.launch {
            val (mode, uuid) = repository.getSavedUserModeAndUUID()
            savedMode = mode
            savedUUID = uuid
            Log.d("SurveillanceVM", "Mode: $mode, UUID: $uuid")
        }
    }

    fun generateUniqueId(): String {
        return UUID.randomUUID().toString()
    }

    fun saveUniqueIdToFirebase(uniqueId: String) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("surveillance_devices").child(uniqueId).setValue(mapOf("status" to "active"))
        Log.d("Firebase", "ID saved successfully")
    }
}

