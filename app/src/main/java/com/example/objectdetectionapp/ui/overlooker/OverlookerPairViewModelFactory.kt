package com.example.objectdetectionapp.ui.overlooker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.objectdetectionapp.data.firebase.FCMService
import com.example.objectdetectionapp.data.firebase.FirebaseService
import com.example.objectdetectionapp.data.firebase.FirebaseServiceImpl
import com.example.objectdetectionapp.data.repository.NotificationRepository
import com.example.objectdetectionapp.data.repository.MainRepository
import com.example.objectdetectionapp.domain.usecases.PairOverlookerWithSurveillanceUseCase

class OverlookerPairViewModelFactory(
    private  val context : Context,
    private val overlookerUUID: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val fcmService = FCMService(context)
        val firebaseService: FirebaseService = FirebaseServiceImpl()
        val mainRepository = MainRepository(context, FirebaseServiceImpl())
        val notificationRepository = NotificationRepository(fcmService,firebaseService)
        val useCase = PairOverlookerWithSurveillanceUseCase(firebaseService, mainRepository, notificationRepository)
        return OverlookerPairViewModel(overlookerUUID, useCase,notificationRepository,mainRepository) as T
    }
}