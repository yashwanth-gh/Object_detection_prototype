package com.example.objectdetectionapp.domain.usecases

import android.graphics.Bitmap
import com.example.objectdetectionapp.data.models.Detection
import com.example.objectdetectionapp.data.repository.DetectionRepository
import com.example.objectdetectionapp.utils.Resource
import kotlinx.coroutines.flow.Flow

class SaveDetectionUseCase(private val detectionRepository: DetectionRepository) {
    suspend fun execute(detection: Detection, imageBitmap: Bitmap?, surveillanceUUID:String): Flow<Resource<String>> {
        return detectionRepository.saveDetectionData(detection, imageBitmap, surveillanceUUID)
    }
}