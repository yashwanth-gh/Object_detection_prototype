package com.example.objectdetectionapp.ui.shared

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.objectdetectionapp.data.repository.UserPreferencesRepository
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID

class ModeSelectionViewModel(private val repository: UserPreferencesRepository) : ViewModel() {


    private val _mode = MutableStateFlow<String?>(null)
    val mode = _mode.asStateFlow()

    private val _uuid = MutableStateFlow<String?>(null)
    val uuid = _uuid.asStateFlow()

//    private val firebaseRef = FirebaseDatabase.getInstance().reference
    private val _tag = "ModeSelectionVM"

    init {
        viewModelScope.launch {
            combine(repository.userMode, repository.userUUID) { savedMode, savedUUID ->
                Pair(savedMode, savedUUID)
            }.collect { (savedMode, savedUUID) ->
                _mode.value = savedMode
                _uuid.value = savedUUID
                Log.d(_tag, "Loaded from DataStore -> mode: $savedMode, uuid: $savedUUID")
            }
        }
    }

//    fun setMode(mode: String) {
//        viewModelScope.launch {
//            if (mode == "surveillance") {
//                val newUUID = UUID.randomUUID().toString()
//
//                // Save to Firebase
////                firebaseRef.child("surveillance_devices")
////                    .child(newUUID)
////                    .setValue(mapOf("status" to "active"))
////                    .addOnSuccessListener {
////                        Log.d(_tag, "UUID $newUUID saved to Firebase")
////                    }
////                    .addOnFailureListener { e ->
////                        Log.e(_tag, "Firebase error: ${e.message}")
////                    }
//
//                // Save to DataStore
//                repository.saveUserMode(mode, newUUID)
//                _uuid.value = newUUID
//            } else {
//                // Save just the mode
//                repository.saveUserMode(mode)
//            }
//
//            _mode.value = mode
//            Log.d(_tag, "Mode set to $mode")
//        }
//    }

    fun setMode(mode: String) {
        viewModelScope.launch {
            if (mode == "surveillance") {
                val newUUID = UUID.randomUUID().toString()

                try {
                    repository.saveModeWithFirebase(mode, newUUID)
                    _uuid.value = newUUID
                } catch (e: Exception) {
                    Log.e(_tag, "Failed to save to Firebase: ${e.message}")
                    return@launch
                }
            } else {
                repository.saveUserMode(mode)
            }

            _mode.value = mode
        }
    }
}