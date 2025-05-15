package com.example.objectdetectionapp.data.services

import android.content.Context

interface SoundDetector {
    fun startListening(context: Context, onSoundDetected: () -> Unit)
    fun stopListening()
}