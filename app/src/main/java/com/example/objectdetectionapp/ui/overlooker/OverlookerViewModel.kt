package com.example.objectdetectionapp.ui.overlooker

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.objectdetectionapp.data.models.SurveillanceDevice
import com.example.objectdetectionapp.data.repository.MainRepository
import com.example.objectdetectionapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OverlookerHomeViewModel(
    private val repository: MainRepository
) : ViewModel() {

    private var _connectedUUID = MutableStateFlow("")
    var connectedUUID: StateFlow<String?> = _connectedUUID.asStateFlow()

    private val _deviceData = MutableStateFlow<Resource<SurveillanceDevice>>(Resource.Loading())
    val deviceData: StateFlow<Resource<SurveillanceDevice>> = _deviceData.asStateFlow()

    private val _isOverlookerValid = MutableStateFlow<Resource<Boolean>>(Resource.Loading())
    val isOverlookerValid: StateFlow<Resource<Boolean>> = _isOverlookerValid.asStateFlow()

    init {
        viewModelScope.launch {
            repository.connectedSurveillanceUUID.collect { uuid ->
                if (uuid != null) {
                    _connectedUUID.value = uuid
                    getSurveillanceDeviceData(uuid)
                }
                Log.d("SurveillanceVM", "UUID: $uuid")
            }
        }
    }

    fun checkOverlookerValidity(overlookerUUID: String, surveillanceUUID: String) {
        viewModelScope.launch {
            _isOverlookerValid.value = Resource.Loading()
            val result = repository.isOverlookerPaired(surveillanceUUID, overlookerUUID)
            _isOverlookerValid.value = result
        }
    }

    private fun getSurveillanceDeviceData(uuid: String) {
        viewModelScope.launch {
            _deviceData.value = Resource.Loading() // Set loading state
            val result = repository.fetchSurveillanceDevice(uuid)
            _deviceData.value = result // Update with the result (Success or Error)
        }
    }

    fun clearLocalDataAndNavigateToModeSelection() {
        viewModelScope.launch {
            repository.clearUserData()
        }
    }
}