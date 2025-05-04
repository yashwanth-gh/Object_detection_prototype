package com.example.objectdetectionapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.objectdetectionapp.data.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(private val repository: MainRepository) : ViewModel() {
    private val _userMode = MutableStateFlow<String?>(null)
    val userMode: StateFlow<String?> = _userMode

    private val _userUUID = MutableStateFlow<String?>(null)
    val userUUID: StateFlow<String?> = _userUUID

    private val _connectedSurveillanceUUID = MutableStateFlow<String?>(null)
    val connectedSurveillanceUUID: StateFlow<String?> = _connectedSurveillanceUUID

    init {
        viewModelScope.launch {
            repository.userMode.collectLatest { mode ->
                _userMode.value = mode
            }
        }
        viewModelScope.launch {
            repository.userUUID.collectLatest { uuid ->
                _userUUID.value = uuid
            }
        }

        viewModelScope.launch {
            repository.connectedSurveillanceUUID.collectLatest { uuid ->
                _connectedSurveillanceUUID.value = uuid
            }
        }
    }
}