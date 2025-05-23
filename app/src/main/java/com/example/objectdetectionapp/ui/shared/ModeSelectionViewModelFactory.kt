package com.example.objectdetectionapp.ui.shared

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.objectdetectionapp.data.firebase.FirebaseServiceImpl
import com.example.objectdetectionapp.data.repository.MainRepository
import com.example.objectdetectionapp.data.repository.SignInRepository

class ModeSelectionViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val firebaseService = FirebaseServiceImpl()
        val signInRepository = SignInRepository(context,firebaseService)
        val repository = MainRepository(context, firebaseService)

        @Suppress("UNCHECKED_CAST")
        return ModeSelectionViewModel(repository,signInRepository) as T
    }
}