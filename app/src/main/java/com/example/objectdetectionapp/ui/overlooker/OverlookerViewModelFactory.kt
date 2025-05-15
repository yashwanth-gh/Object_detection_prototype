package com.example.objectdetectionapp.ui.overlooker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.objectdetectionapp.data.firebase.FirebaseServiceImpl
import com.example.objectdetectionapp.data.repository.DetectionCoordinatorRepository
import com.example.objectdetectionapp.data.repository.MainRepository
import com.example.objectdetectionapp.data.services.FlashlightServiceImpl
import com.example.objectdetectionapp.data.services.SoundDetectorImpl

class OverlookerHomeViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val firebaseService = FirebaseServiceImpl()
        val repository = MainRepository(context,firebaseService)
        val flashlightService = FlashlightServiceImpl(context)
        val soundDetector = SoundDetectorImpl()
        val detectionCoordinatorRepository = DetectionCoordinatorRepository(flashlightService,soundDetector,firebaseService)
        if (modelClass.isAssignableFrom(OverlookerHomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OverlookerHomeViewModel(repository,detectionCoordinatorRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}