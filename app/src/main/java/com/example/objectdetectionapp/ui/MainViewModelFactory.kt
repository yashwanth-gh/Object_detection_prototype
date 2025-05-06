package com.example.objectdetectionapp.ui

import android.content.Context
import com.example.objectdetectionapp.data.firebase.FirebaseService
import com.example.objectdetectionapp.data.firebase.FirebaseServiceImpl
import com.example.objectdetectionapp.data.repository.MainRepository
import com.example.objectdetectionapp.data.repository.SignInRepository

class MainViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        val firebaseService = FirebaseServiceImpl()
        val repository = MainRepository(context, firebaseService)
        val signInRepository = SignInRepository(context, firebaseService)
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository,signInRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}