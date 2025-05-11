package com.example.objectdetectionapp.data.models

data class BoundingBox(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

data class Detection(
    val id: String? = null,
    val timestamp: Long,
    val label: String,
    val confidence: Float,
    val imagePath: String?, // Will be the Firebase Storage URL
    val boundingBox: BoundingBox
)
