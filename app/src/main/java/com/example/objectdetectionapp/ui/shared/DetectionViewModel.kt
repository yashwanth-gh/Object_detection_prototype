package com.example.objectdetectionapp.ui.shared

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.objectdetectionapp.data.models.Detection
import com.example.objectdetectionapp.data.repository.DetectionRepository
import com.example.objectdetectionapp.data.repository.MainRepository
import com.example.objectdetectionapp.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class DetectionViewModel(
    private val detectionRepository: DetectionRepository,
    private val mainRepository: MainRepository
) : ViewModel() {
    private val _detections = MutableStateFlow<List<Detection>>(emptyList())
    val detections: StateFlow<List<Detection>> = _detections

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading


    init {
        fetchDetections()
    }

    private fun fetchDetections() {
        viewModelScope.launch {
            mainRepository.userMode.collectLatest { mode ->
                mainRepository.userUUID.collectLatest { userUUID ->
                    mainRepository.connectedSurveillanceUUID.collectLatest { connectedUUID ->
                        val surveillanceUUID = when (mode) {
                            "surveillance" -> userUUID
                            "overlooker" -> connectedUUID
                            else -> null
                        }

                        surveillanceUUID?.let { uuid ->
                            detectionRepository.getDetectionsForDevice(uuid).collect { resource ->
                                when (resource) {
                                    is Resource.Loading -> _isLoading.value = true
                                    is Resource.Success -> {
                                        _isLoading.value = false
                                        _detections.value = resource.data
                                    }
                                    is Resource.Error -> {
                                        _isLoading.value = false
                                        _detections.value = emptyList()
                                    }
                                }

                            }
                        } ?: run {
                            _detections.value = emptyList()
                            // Optionally handle the case where no relevant UUID is available
                        }
                    }
                }
            }
        }
    }

    fun saveImageToGallery(context: Context, imageUrl: String?) {
        viewModelScope.launch {
            if (imageUrl == null) {
                Toast.makeText(context, "Image URL is null", Toast.LENGTH_SHORT).show()
                return@launch
            }

            try {
                _isLoading.value = true // Indicate loading
                Toast.makeText(context, "Saving image...", Toast.LENGTH_SHORT).show()


                val bitmap = withContext(Dispatchers.IO) {
                    downloadBitmap(imageUrl)
                }

                _isLoading.value = false // Loading finished

                bitmap?.let {
                    saveBitmapToGallery(context, it)
                } ?: run {
                    Toast.makeText(context, "Failed to download image", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                _isLoading.value = false // Ensure loading indicator is off on error
                e.printStackTrace()
                Toast.makeText(context, "Error saving image: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private suspend fun downloadBitmap(imageUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val inputStream: InputStream = connection.inputStream
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveBitmapToGallery(context: Context, bitmap: Bitmap) {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "detection_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/ObjectDetections")
            put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            put(MediaStore.Images.Media.IS_PENDING, 1) // To ensure other apps don't see it until fully written
        }

        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        imageUri?.let { uri ->
            try {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
                Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                resolver.delete(uri, null, null) // Clean up if save failed
                Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        } ?: run {
            Toast.makeText(context, "Failed to insert media item", Toast.LENGTH_SHORT).show()
        }
    }

}