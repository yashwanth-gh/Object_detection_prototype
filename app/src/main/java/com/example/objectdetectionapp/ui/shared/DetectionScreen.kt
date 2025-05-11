package com.example.objectdetectionapp.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.objectdetectionapp.data.models.Detection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape


@Composable
fun DetectionScreen() {
    val context = LocalContext.current

    val detectionViewModel: DetectionViewModel = viewModel(
        factory = DetectionViewModelFactory(context)
    )
    val detections by detectionViewModel.detections.collectAsState()
    val isLoading = detections.isEmpty() && detectionViewModel.isLoading.collectAsState().value
    val userMode by detectionViewModel.userMode.collectAsState()

    var sortMostRecentFirst by remember { mutableStateOf(true) }
    val sortedDetections = remember(detections, sortMostRecentFirst) {
        if (sortMostRecentFirst) {
            detections.sortedByDescending { it.timestamp }
        } else {
            detections.sortedBy { it.timestamp }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .padding(6.dp)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            )
            {

                Text(
                    "Recent detections",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Cursive
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (sortMostRecentFirst) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (sortMostRecentFirst) "Show Oldest First" else "Show Most Recent First",
                        modifier = Modifier
                            .clickable { sortMostRecentFirst = !sortMostRecentFirst }
                            .padding(4.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (sortMostRecentFirst) "Oldest first" else "Recent first",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end =  4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()

            if (detections.isEmpty()) {
                Text(
                    "No detections available.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(sortedDetections) { detection ->
                        DetectionCard(
                            detection = detection,
                            userMode,
                            onDownloadClick = { imageUrl ->
                                detectionViewModel.saveImageToGallery(context, imageUrl)
                            },
                            onDeleteClick = {
                                detectionViewModel.deleteDetection(detection.id)
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun DetectionCard(
    detection: Detection,
    userMode:String?,
    onDownloadClick: (String?) -> Unit,
    onDeleteClick: () -> Unit
) {
    val context = LocalContext.current
    val date = Date(detection.timestamp)
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val confidencePercent = (detection.confidence * 100).toInt()


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Image
            detection.imagePath?.let { imageUrl ->
                val uri = Uri.parse(imageUrl)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(uri)
                            .crossfade(true)
                            .size(Size.ORIGINAL)
                            .build(),
                        contentDescription = "Detection Image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "image/*")
                                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                }
                                context.startActivity(viewIntent)
                            },
                        contentScale = ContentScale.Crop
                    )

                    if (userMode == "surveillance") {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 8.dp, end = 8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Detection",
                                tint = Color.Red,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormatter.format(date), // "08 May 2025"
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = timeFormatter.format(date), // "03:45 PM"
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row: Type + Confidence
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                TypeBadge(type = detection.label)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = " Confidence : $confidencePercent%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val shareText = buildString {
                            append("🚨 Detection Alert 🚨\n\n")
                            append("• Type: ${detection.label.replaceFirstChar { it.uppercaseChar() }}\n")
                            append("• Confidence: $confidencePercent%\n")
                            append("• Date: ${dateFormatter.format(date)}\n")
                            append("• Time: ${timeFormatter.format(date)}\n")
                            append("• Image: ${detection.imagePath ?: "N/A"}\n")
                        }

                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Detection Info", shareText))

                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(
                            Intent.createChooser(
                                shareIntent,
                                "Share Detection Info"
                            )
                        )

                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share")
                }

                Button(
                    onClick = {
                        val detectionText = buildString {
                            append("🚨 Detection Info 🚨\n")
                            append("• Type: ${detection.label}\n")
                            append("• Confidence: $confidencePercent%\n")
                            append("• Date: ${dateFormatter.format(date)}\n")
                            append("• Time: ${timeFormatter.format(date)}\n")
                            append("• Image URL: ${detection.imagePath ?: "N/A"}")
                        }

                        val clipboard =
                            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Detection", detectionText))
                        Toast.makeText(
                            context,
                            "Detection info copied to clipboard",
                            Toast.LENGTH_SHORT
                        ).show()

                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Copy"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { onDownloadClick(detection.imagePath) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "download"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Download", color = MaterialTheme.colorScheme.onPrimary)
            }

        }
    }
}


@Composable
fun TypeBadge(type: String) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "Type: ${type.replaceFirstChar { it.uppercaseChar() }}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }
}
