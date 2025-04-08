package com.example.objectdetectionapp.data.firebase

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FirebaseServiceImpl : FirebaseService {

    private val database = FirebaseDatabase.getInstance().reference
    private val _tag = "FirebaseServiceImpl"

    override suspend fun saveSurveillanceDevice(uuid: String) {
        try {
            database.child("surveillance_devices")
                .child(uuid)
                .setValue(mapOf("status" to "active"))
                .await()
            Log.d(_tag, "UUID $uuid saved to Firebase")
        } catch (e: Exception) {
            Log.e(_tag, "Failed to save UUID: ${e.message}")
            throw e
        }
    }

    override suspend fun isValidSurveillanceUUID(uuid: String): Boolean {
        return try {
            val snapshot = database.child("surveillance_devices").child(uuid).get().await()
            snapshot.exists() && snapshot.child("status").value == "active"
        } catch (e: Exception) {
            Log.e(_tag, "UUID validation failed: ${e.message}")
            false
        }
    }

    override suspend fun addOverlookerToSurveillance(surveillanceUUID: String, overlookerUUID: String) {
        try {
            val path = database.child("surveillance_devices")
                .child(surveillanceUUID)
                .child("overlookers")
                .child(overlookerUUID)

            path.setValue(true).await()
            Log.d(_tag, "Overlooker $overlookerUUID added under $surveillanceUUID")
        } catch (e: Exception) {
            Log.e(_tag, "Failed to add Overlooker: ${e.message}")
            throw e
        }
    }
}