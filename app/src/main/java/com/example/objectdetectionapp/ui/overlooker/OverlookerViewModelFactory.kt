package com.example.objectdetectionapp.ui.overlooker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.objectdetectionapp.data.firebase.FirebaseServiceImpl
import com.example.objectdetectionapp.data.repository.MainRepository

class OverlookerHomeViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val firebaseService = FirebaseServiceImpl()
        val repository = MainRepository(context,firebaseService)
        if (modelClass.isAssignableFrom(OverlookerHomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OverlookerHomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}