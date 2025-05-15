package com.example.objectdetectionapp.ui.surveillance


import android.Manifest
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.graphics.*
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.objectdetectionapp.tflite.BoundingBoxOverlay
import com.example.objectdetectionapp.tflite.EfficientDetLiteDetector
import com.example.objectdetectionapp.ui.components.NavigateWithPermissionAndLoading
import com.example.objectdetectionapp.utils.processImageProxy
import java.io.ByteArrayOutputStream

//import com.example.objectdetectionapp.tflite.TFLiteObjectDetector
/*
@Composable
fun CameraPreviewScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    val objectDetector = remember {
        TFLiteObjectDetector(context)
    }

    val detectionResults = remember { mutableStateListOf<TFLiteObjectDetector.DetectionResult>() }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)

            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                        processImageProxy(imageProxy, objectDetector, detectionResults)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )

            previewView
        }
    )

    // Draw bounding boxes on the detected people
    if (detectionResults.isNotEmpty()) {
        Log.d("CameraPreview", "Creating BoundingBoxOverlay with ${detectionResults.size} results") // ADD THIS LINE
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                BoundingBoxOverlay(
                    ctx,
                    detectionResults.toList(),
                    objectDetector.INPUT_IMAGE_WIDTH, // Access via objectDetector
                    objectDetector.INPUT_IMAGE_HEIGHT // Access via objectDetector
                )
            }
        )
    }
}

private fun processImageProxy(
    imageProxy: ImageProxy,
    detector: TFLiteObjectDetector,
    detectionResults: MutableList<TFLiteObjectDetector.DetectionResult>
) {
    val bitmap = imageProxy.toBitmap()
    if (bitmap != null) {
        val results = detector.detect(bitmap)
        detectionResults.clear()
        detectionResults.addAll(results)
        Log.d("CameraPreview", "Number of detection results: ${detectionResults.size}")
    } else {
        Log.e("CameraPreview", "Error converting ImageProxy to Bitmap: Received null Bitmap.")
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
}*/


@Composable
fun CameraPreviewScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel: SurveillanceViewModel =
        viewModel(factory = SurveillanceViewModelFactory(context)) // Get ViewModel

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var navigateToHome by remember { mutableStateOf(false) }
    val objectDetector = remember {
        EfficientDetLiteDetector(context) // Use EfficientDetLiteDetector
    }
    val surveillanceUUID by viewModel.surveillanceUUID.collectAsState()
    val goToSurveillanceScreen by viewModel.goToSurveillanceHome.collectAsState()


    LaunchedEffect(surveillanceUUID) {
        if (!surveillanceUUID.isNullOrBlank()) {
            viewModel.observeNightMode(surveillanceUUID)
            viewModel.observeStartCamera(surveillanceUUID)
        }
    }
    LaunchedEffect(goToSurveillanceScreen) {
        if (goToSurveillanceScreen) {
            navigateToHome = true
        }
    }

    DisposableEffect(surveillanceUUID) {
        if (!surveillanceUUID.isNullOrBlank()) {
            viewModel.startObservingNightMode(surveillanceUUID!!)
        }
        onDispose {
            viewModel.stopObservingNightMode()
        }
    }


    val detectionResults =
        remember { mutableStateListOf<EfficientDetLiteDetector.DetectionResult>() } // Update the result type

    // Create a background executor for image analysis
    val analysisExecutor =
        remember { ContextCompat.getMainExecutor(context) } // Or a custom ExecutorService

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                // Set a target frame rate if needed
                // .setTargetFrameRate(15)
                .build()
                .also {
                    it.setAnalyzer(analysisExecutor) { imageProxy ->
                        val bitmap = imageProxy.toBitmap() // Get the Bitmap here
                        processImageProxy(imageProxy, objectDetector, detectionResults) { results ->
                            viewModel.handleDetectionResults(
                                results,
                                bitmap
                            )
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
            viewModel.setCameraControl(camera.cameraControl)
            previewView
        }
    )

    // Draw bounding boxes on the detected people
    if (detectionResults.isNotEmpty()) {
        Log.d("CameraPreview", "Creating BoundingBoxOverlay with ${detectionResults.size} results")
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                BoundingBoxOverlay(
                    ctx,
                    detectionResults.toList(),
                    objectDetector.INPUT_IMAGE_WIDTH,
                    objectDetector.INPUT_IMAGE_HEIGHT
                )
            }
        )
    }

    NavigateWithPermissionAndLoading(
        shouldNavigate = navigateToHome,
        onNavigated = { navigateToHome = false },
        destination = "surveillance/${surveillanceUUID}/surveillance",
        navController = navController
    )
}
