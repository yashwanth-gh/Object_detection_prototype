package com.example.objectdetectionapp.ui.surveillance

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.objectdetectionapp.data.models.Overlooker
import com.example.objectdetectionapp.data.repository.MainRepository
import com.example.objectdetectionapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ManageDevicesViewModel(
    private val repository: MainRepository,
    private val surveillanceUUID: String
) : ViewModel() {

    private val _overlookers = MutableStateFlow<Resource<List<Overlooker>>>(Resource.Loading())
    val overlookers: StateFlow<Resource<List<Overlooker>>> = _overlookers.asStateFlow()


    init {
        fetchOverlookers()
    }

    private fun fetchOverlookers() {
        viewModelScope.launch {
            _overlookers.value = Resource.Loading()
            val result = repository.getPairedOverlookers(surveillanceUUID)
            _overlookers.value = result
        }
    }

    fun deleteOverlooker(overlookerUUID: String) {
        viewModelScope.launch {
            val result = repository.deleteOverlooker(surveillanceUUID, overlookerUUID)
            if (result is Resource.Success) {
                // Refresh the list after deletion
                fetchOverlookers()
            } else if (result is Resource.Error) {
                // Handle error if needed
            }
        }
    }



}
