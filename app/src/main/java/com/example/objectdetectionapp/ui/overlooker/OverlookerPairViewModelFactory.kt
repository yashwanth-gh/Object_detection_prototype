package com.example.objectdetectionapp.ui.overlooker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.objectdetectionapp.data.firebase.FirebaseService
import com.example.objectdetectionapp.data.repository.UserPreferencesRepository
import com.example.objectdetectionapp.domain.usecases.PairOverlookerWithSurveillanceUseCase

class OverlookerPairViewModelFactory(
    private val firebaseService: FirebaseService,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val overlookerUUID: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val useCase = PairOverlookerWithSurveillanceUseCase(firebaseService, userPreferencesRepository)
        return OverlookerPairViewModel(overlookerUUID, useCase) as T
    }
}