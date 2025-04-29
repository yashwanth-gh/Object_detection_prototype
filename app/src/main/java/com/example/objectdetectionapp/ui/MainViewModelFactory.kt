package com.example.objectdetectionapp.ui

import android.content.Context
import com.example.objectdetectionapp.data.firebase.FirebaseService
import com.example.objectdetectionapp.data.firebase.FirebaseServiceImpl
import com.example.objectdetectionapp.data.repository.MainRepository

class MainViewModelFactory(private val context: Context) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        val firebaseService = FirebaseServiceImpl()
        val repository = MainRepository(context, firebaseService)
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}