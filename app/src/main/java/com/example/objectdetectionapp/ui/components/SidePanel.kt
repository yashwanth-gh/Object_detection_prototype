package com.example.objectdetectionapp.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.objectdetectionapp.R

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun SidePanel(
    mode: String? = "No Mode",
    uuid: String? = "NaN",
    onSettingsClick: () -> Unit,
) {
    val context = LocalContext.current

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start // Align content to the start
        ) {
            // Dummy Profile Picture
            Image(
                painter = painterResource(id = R.drawable.ic_profile_placeholder_foreground),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(72.dp) // Slightly smaller profile image
                    .clip(CircleShape)
                    .align(Alignment.CenterHorizontally), // Center the image
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Dummy User Name
            Text(
                text = "Hi! User",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterHorizontally) // Center the name
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Display Mode and UUID if available
            if (!mode.isNullOrBlank() && mode != "No Mode" && !uuid.isNullOrBlank() && uuid != "NaN") {
                Text(text = "Mode:", style = MaterialTheme.typography.bodyMedium)
                Text(text = mode, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "UUID:", style = MaterialTheme.typography.bodyMedium)
                Text(text = uuid, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(16.dp))
            }

            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // GitHub Link as TextButton
            TextButton(onClick = { openLink(context, "https://github.com/yashwanth-gh/Object_detection_prototype") }) {
                Text(
                    text = "GitHub Repo",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary // Use primary color for emphasis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            // Settings Link as TextButton with Icon
            TextButton(onClick = onSettingsClick) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.weight(1f)) // Push other links to the bottom

            // Optional Links
            TextButton(onClick = { /* TODO: Implement Privacy Policy */ }) {
                Text("Privacy Policy", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
            }
            TextButton(onClick = { /* TODO: Implement Terms of Service */ }) {
                Text("Terms of Service", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

private fun openLink(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}