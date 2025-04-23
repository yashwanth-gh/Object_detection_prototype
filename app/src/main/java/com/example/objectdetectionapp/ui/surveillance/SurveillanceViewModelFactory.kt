package com.example.objectdetectionapp.ui.surveillance

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.objectdetectionapp.data.firebase.FCMService
import com.example.objectdetectionapp.data.firebase.FirebaseServiceImpl
import com.example.objectdetectionapp.data.repository.NotificationRepository
import com.example.objectdetectionapp.data.repository.MainRepository



class SurveillanceViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val firebaseService = FirebaseServiceImpl()
        val fcmService = FCMService(context)
        val repository = MainRepository(context, firebaseService)
        val notificationRepository = NotificationRepository(fcmService,firebaseService)

        @Suppress("UNCHECKED_CAST")
        return SurveillanceViewModel(repository,notificationRepository) as T
    }
}