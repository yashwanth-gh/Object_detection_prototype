package com.example.objectdetectionapp.ui.shared

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.objectdetectionapp.data.repository.UserPreferencesRepository
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

    fun setMode(mode: String, uuid: String) {
        viewModelScope.launch {
            if (mode == "surveillance") {
                try {
                    repository.saveModeWithFirebase(mode, uuid)
                    _uuid.value = uuid
                } catch (e: Exception) {
                    Log.e(_tag, "Failed to save to Firebase: ${e.message}")
                    return@launch
                }
            } else {
                repository.saveUserMode(mode, uuid)
                _uuid.value = uuid
            }

            _mode.value = mode
        }
    }
}