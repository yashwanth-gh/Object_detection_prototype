package com.example.objectdetectionapp.ui.surveillance

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.objectdetectionapp.data.firebase.FCMService
import com.example.objectdetectionapp.data.firebase.FirebaseDataStorage
import com.example.objectdetectionapp.data.firebase.FirebaseServiceImpl
import com.example.objectdetectionapp.data.repository.DetectionCoordinatorRepository
import com.example.objectdetectionapp.data.repository.DetectionRepositoryImpl
import com.example.objectdetectionapp.data.repository.NotificationRepository
import com.example.objectdetectionapp.data.repository.MainRepository
import com.example.objectdetectionapp.data.services.FlashlightService
import com.example.objectdetectionapp.data.services.FlashlightServiceImpl
import com.example.objectdetectionapp.data.services.SoundDetectorImpl
import com.example.objectdetectionapp.domain.usecases.SaveDetectionUseCase
import com.example.objectdetectionapp.utils.SoundManager


class SurveillanceViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val firebaseDataStorage = FirebaseDataStorage()
        val firebaseService = FirebaseServiceImpl()
        val fcmService = FCMService(context)
        val repository = MainRepository(context, firebaseService)
        val notificationRepository = NotificationRepository(fcmService,firebaseService)
        val detectionRepository = DetectionRepositoryImpl(firebaseDataStorage,firebaseService, context)
        val saveDetectionUseCase = SaveDetectionUseCase(detectionRepository)
        val soundManager = SoundManager(context.applicationContext)
        val flashlightService = FlashlightServiceImpl(context)
        val soundDetector = SoundDetectorImpl()
        val detectionCoordinatorRepository = DetectionCoordinatorRepository(flashlightService,soundDetector,firebaseService)

        @Suppress("UNCHECKED_CAST")
        return SurveillanceViewModel(context,repository,notificationRepository,saveDetectionUseCase,soundManager,detectionCoordinatorRepository) as T
    }
}