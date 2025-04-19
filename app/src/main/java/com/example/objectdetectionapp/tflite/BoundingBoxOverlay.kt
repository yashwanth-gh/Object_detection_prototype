package com.example.objectdetectionapp.tflite

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.View


class BoundingBoxOverlay(
    context: Context,
//    private val detectionResults: List<TFLiteObjectDetector.DetectionResult>,
    private val detectionResults: List<EfficientDetLiteDetector.DetectionResult>,
    private val imageWidth: Int,
    private val imageHeight: Int
) : View(context) {
    constructor(context: Context, attrs: android.util.AttributeSet? = null) : this(context, emptyList(), 0, 0)
    constructor(context: Context, attrs: android.util.AttributeSet? = null, defStyleAttr: Int = 0) : this(context, emptyList(), 0, 0)

    private val paint = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 10f
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint().apply {
        color = Color.RED
        textSize = 40f // Adjust text size as needed
        style = Paint.Style.FILL
        textAlign = Paint.Align.LEFT
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d("BoundingBoxOverlay", "onDraw called with ${detectionResults.size} results - Width: $width, Height: $height, ImageWidth: $imageWidth, ImageHeight: $imageHeight") // Added more logs
        val viewWidth = width.toFloat()
        val viewHeight = height.toFloat()

        detectionResults.forEach { result ->
            val rect = result.boundingBox
            // Assuming rect coordinates are normalized [0, 1]
            val left = rect.left * viewWidth
            val top = rect.top * viewHeight
            val right = rect.right * viewWidth
            val bottom = rect.bottom * viewHeight

            canvas.drawRect(left, top, right, bottom, paint)
            canvas.drawText(
                result.label, // Use the label from the DetectionResult
                left,
                top - textPaint.textSize, // Position the text above the box
                textPaint
            )
        }
    }
}