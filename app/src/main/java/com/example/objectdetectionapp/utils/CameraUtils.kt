package com.example.objectdetectionapp.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.util.Log
import androidx.camera.core.ImageProxy
import com.example.objectdetectionapp.tflite.EfficientDetLiteDetector
import java.io.ByteArrayOutputStream

fun processImageProxy(
    imageProxy: ImageProxy,
    detector: EfficientDetLiteDetector, // Use EfficientDetLiteDetector
    detectionResults: MutableList<EfficientDetLiteDetector.DetectionResult>, // Update the result type
    onDetectionComplete: (List<EfficientDetLiteDetector.DetectionResult>) -> Unit // Add a callback
) {
    val bitmap = imageProxy.toBitmap()
    if (bitmap != null) {
        val results = detector.detect(bitmap)
        detectionResults.clear()
        detectionResults.addAll(results)
        Log.d("CameraPreview", "Number of detection results (EfficientDet): ${detectionResults.size}")
        onDetectionComplete(results) // Invoke the callback with the results
    } else {
        Log.e("CameraPreview", "Error converting ImageProxy to Bitmap.")
    }
    imageProxy.close()
}

fun ImageProxy.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(
        nv21,
        ImageFormat.NV21,
        width, height,
        null
    )
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}