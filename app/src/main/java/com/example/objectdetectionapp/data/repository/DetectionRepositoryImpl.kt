package com.example.objectdetectionapp.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.example.objectdetectionapp.data.firebase.FirebaseDataStorage
import com.example.objectdetectionapp.data.firebase.FirebaseService
import com.example.objectdetectionapp.data.models.Detection
import com.example.objectdetectionapp.utils.Resource
import com.google.firebase.database.DatabaseError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.ByteArrayOutputStream
import java.util.UUID

class DetectionRepositoryImpl(
    private val firebaseDataStorage: FirebaseDataStorage,
    private val firebaseService: FirebaseService,
    private val context: Context
) : DetectionRepository {

    private val _tag = "DetectionRepo"

    override suspend fun saveDetectionData(
        detection: Detection,
        imageBitmap: Bitmap?,
        surveillanceUUID: String
    ): Flow<Resource<String>> = flow {
        if (imageBitmap == null) {
            val detectionId = firebaseDataStorage.saveDetectionMetadata(detection, surveillanceUUID)
            emit(Resource.Success(detectionId))
        } else {
            val imageName = "detection_image_${UUID.randomUUID()}.jpg"
            val byteArrayOutputStream = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val imageData = byteArrayOutputStream.toByteArray()

            emit(Resource.Loading<String>()) // ✅ must provide the type here

            val imageUrlResult = firebaseDataStorage.uploadImageToStorage(imageName, imageData)
            imageUrlResult.fold(
                onSuccess = { imageUrl ->
                    val detectionWithImageUrl = detection.copy(imagePath = imageUrl)
                    val detectionId = firebaseDataStorage.saveDetectionMetadata(
                        detectionWithImageUrl,
                        surveillanceUUID
                    )
                    emit(Resource.Success(detectionId))
                },
                onFailure = { error ->
                    Log.e(_tag, "Error uploading image: $error")
                    emit(Resource.Error<String>(error)) // ✅ must provide the type here
                }
            )
        }
    }.catch { error ->
        Log.e(_tag, "General error saving detection data: $error")
        emit(Resource.Error<String>(error))
    }.flowOn(Dispatchers.IO)


    override suspend fun getDetectionsForDevice(surveillanceUUID: String): Flow<Resource<List<Detection>>> =
        firebaseService.fetchDetectionDetails(surveillanceUUID)
            .map<List<Detection>, Resource<List<Detection>>> { detections ->
                Resource.Success(detections)
            }
            .catch { error ->
                Log.e(_tag, "Error fetching detections from Firebase Service: ${error.message}")
                emit(Resource.Error<List<Detection>>(Throwable("Error fetching detections: ${error.message}")))
            }
            .flowOn(Dispatchers.IO)

    override suspend fun deleteDetection(surveillanceUUID: String, detectionId: String): Flow<Resource<Void?>> = flow {
        emit(Resource.Loading())
        try {
            firebaseService.deleteDetection(surveillanceUUID, detectionId)
            emit(Resource.Success(null as Void?)) // Explicitly cast null to Void?
        } catch (e: Exception) {
            Log.e(_tag, "Error deleting detection: ${e.message}")
            emit(Resource.Error(e))
        }
    }.flowOn(Dispatchers.IO)
}

