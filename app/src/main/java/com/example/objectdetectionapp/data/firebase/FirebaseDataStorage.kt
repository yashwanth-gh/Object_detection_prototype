package com.example.objectdetectionapp.data.firebase

import com.example.objectdetectionapp.data.models.Detection
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseDataStorage {

    private val storage = FirebaseStorage.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    private val dBRef = database.child("surveillance_devices")

    suspend fun uploadImageToStorage(imageName: String, imageData: ByteArray): Result<String> {
        return try {
            val storageRef = storage.reference.child("detection_images/$imageName")
            val uploadTask = storageRef.putBytes(imageData).await()
            if (uploadTask.task.isSuccessful) {
                val downloadUrl = storageRef.downloadUrl.await().toString()
                Result.success(downloadUrl)
            } else {
                Result.failure(uploadTask.task.exception ?: Exception("Failed to upload image."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveDetectionMetadata(detection: Detection, surveillanceUUID: String): String {
        val detectionId = UUID.randomUUID().toString()

        return try {
            val detectionRef = dBRef
                .child(surveillanceUUID)
                .child("detections")
                .child(detectionId)

            detectionRef.setValue(detection).await()
            detectionId
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

}