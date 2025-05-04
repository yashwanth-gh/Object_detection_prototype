package com.example.objectdetectionapp.data.repository

import android.graphics.Bitmap
import com.example.objectdetectionapp.data.models.Detection
import com.example.objectdetectionapp.utils.Resource
import kotlinx.coroutines.flow.Flow

interface DetectionRepository {
    suspend fun saveDetectionData(detection: Detection, imageBitmap: Bitmap?, surveillanceUUID:String): Flow<Resource<String>>
    suspend fun getDetectionsForDevice(surveillanceUUID: String): Flow<Resource<List<Detection>>>
}