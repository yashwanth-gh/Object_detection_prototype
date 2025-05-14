package com.example.objectdetectionapp.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.objectdetectionapp.data.repository.MainRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SettingsViewModel(private val repository: MainRepository) : ViewModel() {

    private val _notificationCooldown = MutableStateFlow(3L) // default 3 minutes
    val notificationCooldown: StateFlow<Long> = _notificationCooldown.asStateFlow()

    private val _saveCooldown = MutableStateFlow(3L)
    val saveCooldown: StateFlow<Long> = _saveCooldown.asStateFlow()

    private val _soundCooldown = MutableStateFlow(10L) // default 10 seconds
    val soundCooldown: StateFlow<Long> = _soundCooldown.asStateFlow()

    private val _emailCooldown = MutableStateFlow(3L) // default 3 minutes
    val emailCooldown: StateFlow<Long> = _emailCooldown.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getSurveillanceSettings().collect { settings ->
                _notificationCooldown.value = TimeUnit.MILLISECONDS.toMinutes(settings.notificationInterval)
                _saveCooldown.value = TimeUnit.MILLISECONDS.toMinutes(settings.saveInterval)
                _soundCooldown.value = TimeUnit.MILLISECONDS.toSeconds(settings.soundInterval)
                _emailCooldown.value = TimeUnit.MILLISECONDS.toMinutes(settings.emailInterval)
            }
        }
    }


    fun updateNotificationCooldown(minutes: Long) {
        _notificationCooldown.value = minutes
        saveAllSettings()
    }

    fun updateSaveCooldown(minutes: Long) {
        _saveCooldown.value = minutes
        saveAllSettings()
    }

    fun updateSoundCooldown(seconds: Long) {
        _soundCooldown.value = seconds
        saveAllSettings()
    }

    fun updateEmailCooldown(minutes: Long) {
        _emailCooldown.value = minutes
        saveAllSettings()
    }


    private fun saveAllSettings() {
        viewModelScope.launch {
            repository.saveSurveillanceSettings(
                notificationIntervalMs = TimeUnit.MINUTES.toMillis(_notificationCooldown.value),
                saveIntervalMs = TimeUnit.MINUTES.toMillis(_saveCooldown.value),
                soundIntervalMs = TimeUnit.SECONDS.toMillis(_soundCooldown.value),
                emailIntervalMs = TimeUnit.MINUTES.toMillis(_emailCooldown.value)
            )
        }
    }

}
