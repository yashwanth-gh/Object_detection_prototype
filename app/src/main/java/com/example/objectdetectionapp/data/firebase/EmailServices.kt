package com.example.objectdetectionapp.data.firebase

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.objectdetectionapp.data.models.SurveillanceDevice
import com.example.objectdetectionapp.tflite.EfficientDetLiteDetector
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object EmailServices {

    private val functions: FirebaseFunctions = Firebase.functions

    fun sendEmail(
        recipients: List<String>,
        deviceData: SurveillanceDevice,
        personDetections: List<EfficientDetLiteDetector.DetectionResult>,
        image: Bitmap?
    ) {
        val subject = "Surveillance Alert from ${deviceData.user.username}"
        val body = buildDetectionEmailBody(personDetections)
        val imageBase64 = image?.let { convertBitmapToBase64(it) }



        val payload = JSONObject().apply {
            put("recipients", JSONArray(recipients))
            put("subject", subject)
            put("body", body)
            put("imageBase64", imageBase64)
        }
        Log.d("EmailService", "Attempting to send email")

        Log.d("EmailService", "$payload")

        val requestBody = payload.toString()
            .toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("https://us-central1-object-detection-app-prototype.cloudfunctions.net/sendEmail")
            .post(requestBody)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("EmailService", "❌ Failed to send email: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("EmailService", "✅ Email sent successfully via HTTP function")
                } else {
                    Log.e("EmailService", "❌ Error response from server: ${response.code}")
                }
            }
        })
    }


    private fun buildDetectionEmailBody(
        detections: List<EfficientDetLiteDetector.DetectionResult>,
        detectionTimes: List<Long> = detections.map { System.currentTimeMillis() }
    ): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        if (detections.isEmpty()) {
            val currentTime = sdf.format(Date())
            return """
            <html>
            <body>
            <h2>👋 Hello,</h2>
            <p>No person was detected during the monitoring period.</p>
            <p>Report generated at: $currentTime</p>
            <p>Regards,<br>Your Security System</p>
            </body>
            </html>
        """.trimIndent()
        }

        val detectionHtml = buildString {
            appendLine("<html>")
            appendLine("<body>")
            appendLine("<h2>👋 Hello,</h2>")
            appendLine("<p>The security system has detected the following:</p>")
            appendLine("<h3>🔍 Person Detection Report:</h3>")

            detections.forEachIndexed { index, detection ->
                val time = sdf.format(Date(detectionTimes[index]))
                appendLine("<div style='margin-bottom: 20px; padding: 10px; border: 1px solid #ddd; border-radius: 5px;'>")
                appendLine("<h4>🧍 Person ${index + 1}:</h4>")
                appendLine("<ul>")
                appendLine("<li><b>Label:</b> ${detection.label}</li>")
                appendLine("<li><b>Confidence:</b> ${String.format("%.2f", detection.confidence * 100)}%</li>")
                appendLine("<li><b>Detection Time:</b> $time</li>")
                appendLine("</ul>")
                appendLine("</div>")
            }

            val reportTime = sdf.format(Date())
            appendLine("<p>Report generated at: $reportTime</p>")
            appendLine("<p>Regards,<br>Your Security System</p>")
            appendLine("</body>")
            appendLine("</html>")
        }

        return detectionHtml
    }


    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}