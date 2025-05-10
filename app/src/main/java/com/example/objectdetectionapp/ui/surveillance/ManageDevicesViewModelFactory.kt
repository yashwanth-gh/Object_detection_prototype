package com.example.objectdetectionapp.ui.surveillance

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.objectdetectionapp.data.firebase.FirebaseServiceImpl
import com.example.objectdetectionapp.data.repository.MainRepository

class ManageDevicesViewModelFactory(
    private val context: Context,
    private val surveillanceUUID: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val firebaseService = FirebaseServiceImpl()
        val repository = MainRepository(context, firebaseService)
        return ManageDevicesViewModel(repository, surveillanceUUID) as T
    }
}
