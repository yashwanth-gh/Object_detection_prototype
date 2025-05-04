package com.example.objectdetectionapp.ui.shared

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.objectdetectionapp.data.firebase.FirebaseDataStorage
import com.example.objectdetectionapp.data.firebase.FirebaseServiceImpl
import com.example.objectdetectionapp.data.models.Detection
import com.example.objectdetectionapp.data.repository.DetectionRepository
import com.example.objectdetectionapp.data.repository.DetectionRepositoryImpl
import com.example.objectdetectionapp.data.repository.MainRepository
import com.example.objectdetectionapp.utils.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class DetectionViewModel(
    private val detectionRepository: DetectionRepository,
    private val mainRepository: MainRepository
) : ViewModel() {
    private val _detections = MutableStateFlow<List<Detection>>(emptyList())
    val detections: StateFlow<List<Detection>> = _detections

    init {
        fetchDetections()
    }

    private fun fetchDetections() {
        viewModelScope.launch {
            mainRepository.userMode.collectLatest { mode ->
                mainRepository.userUUID.collectLatest { userUUID ->
                    mainRepository.connectedSurveillanceUUID.collectLatest { connectedUUID ->
                        val surveillanceUUID = when (mode) {
                            "surveillance" -> userUUID
                            "overlooker" -> connectedUUID
                            else -> null
                        }

                        surveillanceUUID?.let { uuid ->
                            detectionRepository.getDetectionsForDevice(uuid).collect { resource ->
                                when (resource) {
                                    is Resource.Loading -> {
                                        // Optionally handle loading state in UI
                                    }
                                    is Resource.Success -> _detections.value = resource.data
                                    is Resource.Error -> {
                                        // Optionally handle error state in UI (e.g., show a message)
                                        _detections.value = emptyList()
                                    }
                                }
                            }
                        } ?: run {
                            _detections.value = emptyList()
                            // Optionally handle the case where no relevant UUID is available
                        }
                    }
                }
            }
        }
    }

    // ViewModel Factory (for creating DetectionViewModel with dependencies)
    companion object {
        fun provideFactory(
            context:Context
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val firebaseDataStorage = FirebaseDataStorage()
                val firebaseService = FirebaseServiceImpl()
                val detectionRepository = DetectionRepositoryImpl(firebaseDataStorage,firebaseService, context)
                val repository = MainRepository(context, firebaseService)
                if (modelClass.isAssignableFrom(DetectionViewModel::class.java)) {
                    return DetectionViewModel(detectionRepository, repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}