package com.example.objectdetectionapp.utils

import android.content.Context
import android.media.MediaPlayer
import com.example.objectdetectionapp.R

class SoundManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null

    fun playPersonDetectedSound() {
        stopSoundIfPlaying() // Avoid overlapping playback
        mediaPlayer = MediaPlayer.create(context, R.raw.alarm)
        mediaPlayer?.start()
    }

    private fun stopSoundIfPlaying() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
                it.release()
            }
        }
        mediaPlayer = null
    }
}