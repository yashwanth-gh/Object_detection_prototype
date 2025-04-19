package com.example.objectdetectionapp.tflite

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

class EfficientDetLiteDetector(context: Context) {

    val INPUT_IMAGE_HEIGHT: Int = 512 // As per the model description
    val INPUT_IMAGE_WIDTH: Int = 512  // As per the model description
    private val interpreter: Interpreter
    private val labels: List<String> // We'll load these manually for now

    companion object {
        private const val MODEL_FILENAME = "efficientdet-lite3-detection-default_1.tflite"
        private const val LABEL_FILENAME = "labelmap_coco.txt" // You'll need to create this
        private const val NUM_DETECTIONS = 25 // As per the model description
    }

    init {
        val model = FileUtil.loadMappedFile(context, MODEL_FILENAME)
        interpreter = Interpreter(model)
        labels = FileUtil.loadLabels(context, LABEL_FILENAME)
        Log.d("EfficientDetLite", "Loaded model: $MODEL_FILENAME with ${labels.size} labels.")
    }

    data class DetectionResult(
        val label: String,
        val confidence: Float,
        val boundingBox: RectF
    )

    fun detect(bitmap: Bitmap): List<DetectionResult> {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_IMAGE_WIDTH, INPUT_IMAGE_HEIGHT, true)
        val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)

        val outputLocations = Array(1) { Array(NUM_DETECTIONS) { FloatArray(4) } }
        val outputClasses = Array(1) { FloatArray(NUM_DETECTIONS) }
        val outputScores = Array(1) { FloatArray(NUM_DETECTIONS) }
        val numDetections = FloatArray(1) // The model outputs this

        val outputs = mapOf(
            0 to outputLocations,
            1 to outputClasses,
            2 to outputScores,
            3 to numDetections
        )

        interpreter.runForMultipleInputsOutputs(arrayOf(inputBuffer), outputs)

        val results = mutableListOf<DetectionResult>()
        val count = numDetections[0].toInt()

        for (i in 0 until count) {
            val score = outputScores[0][i]
            if (score > 0.4f) { // Adjust confidence threshold as needed
                val labelIdx = outputClasses[0][i].toInt()
                val label = labels.getOrElse(labelIdx) { "Unknown" }

                if (label.lowercase() == "person") {
                    val loc = outputLocations[0][i]
                    // The output order is [ymin, xmin, ymax, xmax]
                    Log.d("EfficientDetLite", "👤 Person detected with ${"%.2f".format(score * 100)}% confidence - Bounding Box: ymin=${loc[0]}, xmin=${loc[1]}, ymax=${loc[2]}, xmax=${loc[3]}")

                    results.add(
                        DetectionResult(
                            label = label,
                            confidence = score,
                            boundingBox = RectF(loc[1], loc[0], loc[3], loc[2]) // Convert to left, top, right, bottom
                        )
                    )
                }
            }
        }

        return results
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(INPUT_IMAGE_WIDTH * INPUT_IMAGE_HEIGHT * 3) // Allocate for bytes
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(INPUT_IMAGE_WIDTH * INPUT_IMAGE_HEIGHT)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var pixel = 0
        for (i in 0 until INPUT_IMAGE_HEIGHT) {
            for (j in 0 until INPUT_IMAGE_WIDTH) {
                val value = intValues[pixel++]
                // Extract RGB components and put them as bytes (0-255)
                byteBuffer.put((value shr 16 and 0xFF).toByte()) // R
                byteBuffer.put((value shr 8 and 0xFF).toByte())  // G
                byteBuffer.put((value and 0xFF).toByte())        // B
            }
        }
        return byteBuffer.rewind() as ByteBuffer
    }

    fun close() {
        interpreter.close()
    }
}