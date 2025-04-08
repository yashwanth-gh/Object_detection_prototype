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
}