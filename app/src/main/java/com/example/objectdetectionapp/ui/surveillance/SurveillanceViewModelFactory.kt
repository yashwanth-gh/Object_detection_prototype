package com.example.objectdetectionapp.ui.surveillance

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.objectdetectionapp.data.firebase.FirebaseServiceImpl
import com.example.objectdetectionapp.data.repository.UserPreferencesRepository



class SurveillanceViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val firebaseService = FirebaseServiceImpl()
        val repository = UserPreferencesRepository(context, firebaseService)

        @Suppress("UNCHECKED_CAST")
        return SurveillanceViewModel(repository) as T
    }
}