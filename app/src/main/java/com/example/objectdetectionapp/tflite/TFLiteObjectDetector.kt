package com.example.objectdetectionapp.tflite

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TFLiteObjectDetector(context: Context) {

    val INPUT_IMAGE_HEIGHT: Int = 300
    val INPUT_IMAGE_WIDTH: Int = 300
    private val interpreter: Interpreter
    private val labels: List<String>

    companion object {
        private const val MODEL_FILENAME = "ssd_mobilenet_v1.tflite"
        private const val LABEL_FILENAME = "labelmap.txt"

/*         const val INPUT_IMAGE_WIDTH = 300
         const val INPUT_IMAGE_HEIGHT = 300*/
        private const val NUM_DETECTIONS = 10
    }

    init {
        val model = FileUtil.loadMappedFile(context, MODEL_FILENAME)
        interpreter = Interpreter(model)
        labels = FileUtil.loadLabels(context, LABEL_FILENAME)
    }

    // Will add detection logic next
    data class DetectionResult(
        val label: String,
        val confidence: Float,
        val boundingBox: RectF
    )

    fun detect(bitmap: Bitmap): List<DetectionResult> {
        val resizedBitmap = Bitmap.createScaledBitmap(
            bitmap, INPUT_IMAGE_WIDTH, INPUT_IMAGE_HEIGHT, true
        )
        val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)

        val outputLocations = Array(1) { Array(NUM_DETECTIONS) { FloatArray(4) } }
        val outputClasses = Array(1) { FloatArray(NUM_DETECTIONS) }
        val outputScores = Array(1) { FloatArray(NUM_DETECTIONS) }
        val numDetections = FloatArray(1)

        val inputArray = arrayOf(inputBuffer)
        val outputMap = mapOf(
            0 to outputLocations,
            1 to outputClasses,
            2 to outputScores,
            3 to numDetections
        )

        interpreter.runForMultipleInputsOutputs(inputArray, outputMap)

        val results = mutableListOf<DetectionResult>()
        val count = numDetections[0].toInt()

        for (i in 0 until count) {
            val score = outputScores[0][i]
            if (score > 0.4f) {
                val labelIdx = outputClasses[0][i].toInt()
                val label = labels.getOrElse(labelIdx) { "Unknown" }

                if (label == "person") {
                    val loc = outputLocations[0][i]
                    Log.d("TFLiteDetection", "👤 Person detected with ${"%.2f".format(score * 100)}% confidence - Bounding Box: left=${loc[1]}, top=${loc[0]}, right=${loc[3]}, bottom=${loc[2]}") // Modified log

                    results.add(
                        DetectionResult(
                            label = label,
                            confidence = score,
                            boundingBox = RectF(loc[1], loc[0], loc[3], loc[2]) // left, top, right, bottom
                        )
                    )
                }
            }
        }

        return results
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val inputSize = INPUT_IMAGE_WIDTH * INPUT_IMAGE_HEIGHT * 3 * 4 // Float input
        val byteBuffer = ByteBuffer.allocateDirect(inputSize)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(INPUT_IMAGE_WIDTH * INPUT_IMAGE_HEIGHT)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        for (pixelValue in intValues) {
            // Normalize RGB values to [-1.0, 1.0]
            val r = (pixelValue shr 16 and 0xFF) / 255.0f * 2.0f - 1.0f
            val g = (pixelValue shr 8 and 0xFF) / 255.0f * 2.0f - 1.0f
            val b = (pixelValue and 0xFF) / 255.0f * 2.0f - 1.0f
            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }
        byteBuffer.rewind()
        return byteBuffer
    }


}