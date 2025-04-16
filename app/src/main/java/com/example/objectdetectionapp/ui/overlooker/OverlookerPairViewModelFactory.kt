package com.example.objectdetectionapp.ui.overlooker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.objectdetectionapp.data.firebase.FCMService
import com.example.objectdetectionapp.data.firebase.FirebaseService
import com.example.objectdetectionapp.data.repository.NotificationRepository
import com.example.objectdetectionapp.data.repository.UserPreferencesRepository
import com.example.objectdetectionapp.domain.usecases.PairOverlookerWithSurveillanceUseCase

class OverlookerPairViewModelFactory(
    private  val context : Context,
    private val firebaseService: FirebaseService,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val overlookerUUID: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val fcmService = FCMService(context)
        val useCase = PairOverlookerWithSurveillanceUseCase(firebaseService, userPreferencesRepository)
        val notificationRepository = NotificationRepository(fcmService,firebaseService)
        return OverlookerPairViewModel(overlookerUUID, useCase,notificationRepository) as T
    }
}