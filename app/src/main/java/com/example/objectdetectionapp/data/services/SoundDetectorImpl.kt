package com.example.objectdetectionapp.data.services

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import android.Manifest
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*


class SoundDetectorImpl : SoundDetector {
    private var isListening = false
    private var detectionJob: Job? = null
    private var audioRecord: AudioRecord? = null

    override fun startListening(context: Context, onSoundDetected: () -> Unit) {
        if (isListening) return
        isListening = true

        // now we can refer to Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("SoundDetector", "❌ Microphone permission not granted.")
            isListening = false
            return
        }

        detectionJob = CoroutineScope(Dispatchers.Default).launch {
            val bufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
            )

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            val buffer = ShortArray(bufferSize)
            try {
                audioRecord?.startRecording()
                while (isActive && isListening) {
                    if (!isListening) break // Defensive guard in case isListening flips mid-loop

                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    val amplitude = buffer.take(read).maxOfOrNull { it.toInt() } ?: 0

                    if (amplitude > SOUND_THRESHOLD) {
                        Log.d("SoundDetector", "🔊 Sound detected: $amplitude")
                        withContext(Dispatchers.Main) { onSoundDetected() }
                        delay(DETECTION_COOLDOWN)
                    }

                    delay(CHECK_INTERVAL)
                }

            } catch (e: Exception) {
                Log.e("SoundDetector", "❌ Error during audio recording: ${e.message}")
            }finally {
                Log.d("SoundDetector", "🛑 Cleaning up AudioRecord in coroutine.")
                audioRecord?.let { record ->
                    try {
                        if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                            record.stop()
                        } else {

                        }
                    } catch (ise: IllegalStateException) {
                        Log.w("SoundDetector", "🛡️ Ignored stop() failure: ${ise.message}")
                    } finally {
                        record.release()
                    }
                }
                audioRecord = null
            }

        }
    }

    override fun stopListening() {
        Log.d("SoundDetector", "Stopping sound detection and releasing mic.")
        isListening = false
        detectionJob?.cancel()  // triggers the finally block above
        detectionJob = null
        // no direct stop() here; let the coroutine cleanup
    }

    companion object {
        private const val SAMPLE_RATE = 8000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val SOUND_THRESHOLD = 2000
        private const val CHECK_INTERVAL = 100L
        private const val DETECTION_COOLDOWN = 5000L
    }
}