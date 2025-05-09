package com.example.objectdetectionapp.ui.shared

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.objectdetectionapp.data.firebase.FirebaseDataStorage
import com.example.objectdetectionapp.data.firebase.FirebaseServiceImpl
import com.example.objectdetectionapp.data.repository.DetectionRepositoryImpl
import com.example.objectdetectionapp.data.repository.MainRepository

class DetectionViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val firebaseDataStorage = FirebaseDataStorage()
        val firebaseService = FirebaseServiceImpl()
        val detectionRepository = DetectionRepositoryImpl(firebaseDataStorage, firebaseService, context)
        val repository = MainRepository(context, firebaseService)
        if (modelClass.isAssignableFrom(DetectionViewModel::class.java)) {
            return DetectionViewModel(detectionRepository, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}