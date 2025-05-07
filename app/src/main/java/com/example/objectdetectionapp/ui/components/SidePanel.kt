package com.example.objectdetectionapp.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
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
import androidx.navigation.NavHostController
import com.example.objectdetectionapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SidePanel(
    mode: String? = "No Mode",
    uuid: String? = "NaN",
    connectedSurveillanceUUID: String? = "NaN",
    username: String,
    email: String,
    navController: NavHostController,
    onCloseDrawer: () -> Unit
) {
    val context = LocalContext.current

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Profile Section
            ProfileSection(username)

            Spacer(modifier = Modifier.height(16.dp))

            // User Session Info Section
            if (!mode.isNullOrBlank() && mode != "No Mode" && !uuid.isNullOrBlank() && uuid != "NaN") {
                UserSessionInfo(mode, uuid, connectedSurveillanceUUID)
                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Navigation Links
            NavigationLinks(
                mode = mode,
                uuid = uuid,
                connectedSurveillanceUUID = connectedSurveillanceUUID,
                navController = navController,
                onCloseDrawer = onCloseDrawer,
                context = context
            )

            Spacer(modifier = Modifier.weight(1f)) // Push footer links to the bottom

            // Footer Links
            FooterLinks()
        }
    }
}

@Composable
private fun ProfileSection(username: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_user_image_round),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Hi! $username",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun UserSessionInfo(
    mode: String?,
    uuid: String?,
    connectedSurveillanceUUID: String?
) {
    Column {
        InfoRow("Mode:", mode ?: "")
        Spacer(modifier = Modifier.height(8.dp))
        InfoRow("UUID:", uuid ?: "")

        // Show connected surveillance UUID for overlooker mode
        if (mode?.lowercase() == "overlooker" &&
            !connectedSurveillanceUUID.isNullOrBlank() &&
            connectedSurveillanceUUID != "NaN"
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow("Connected Surveillance UUID:", connectedSurveillanceUUID)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun NavigationLinks(
    mode: String?,
    uuid: String?,
    connectedSurveillanceUUID: String?,
    navController: NavHostController,
    onCloseDrawer: () -> Unit,
    context: Context
) {
    // Home Button
    NavigationButton(
        text = "Home",
        icon = Icons.Filled.Home,
        onClick = {
            when {
                mode?.lowercase() == "surveillance" && !uuid.isNullOrBlank() -> {
                    navController.navigate("surveillance/${uuid}/${mode}")
                }
                mode?.lowercase() == "overlooker" && !uuid.isNullOrBlank() &&
                        !connectedSurveillanceUUID.isNullOrBlank() -> {
                    navController.navigate("overlooker_home/${uuid}/${connectedSurveillanceUUID}")
                }
                else -> {
                    Toast.makeText(context, "Missing UUID or mode!", Toast.LENGTH_SHORT).show()
                }
            }
            onCloseDrawer()
        }
    )

    // Detections Button
    NavigationButton(
        text = "Detections",
        icon = Icons.Filled.Notifications,
        onClick = {
            navController.navigate("detections_screen")
            onCloseDrawer()
        }
    )


// GitHub Repo Button
    NavigationButton(
        text = "GitHub Repo",
        icon = Icons.Filled.Info,
        onClick = {
            openLink(context, "https://github.com/yashwanth-gh/Object_detection_prototype")
            onCloseDrawer()
        }
    )

    // Settings Button
    NavigationButton(
        text = "Settings",
        icon = Icons.Filled.Settings,
        onClick = {
            navController.navigate("settings_screen") // Navigate directly
            onCloseDrawer()
        }
    )
}

@Composable
private fun NavigationButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit
) {
    Column {
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = text,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun FooterLinks() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TextButton(onClick = { /* TODO: Implement Privacy Policy */ }) {
            Text(
                "Privacy Policy",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        TextButton(onClick = { /* TODO: Implement Terms of Service */ }) {
            Text(
                "Terms of Service",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

private fun openLink(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}