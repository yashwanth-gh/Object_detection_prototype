package com.example.objectdetectionapp.ui.shared

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object NavigationStateHandler {
    private val _isNavigating = MutableStateFlow(false)
    val isNavigating = _isNavigating.asStateFlow()

    fun startNavigation() {
        _isNavigating.value = true
    }

    fun stopNavigation() {
        _isNavigating.value = false
    }
}