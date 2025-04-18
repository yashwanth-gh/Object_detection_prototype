package com.example.objectdetectionapp.ui.shared

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.objectdetectionapp.ui.components.AppLoadingScreen
import com.example.objectdetectionapp.ui.components.NavigateWithPermissionAndLoading
import java.util.UUID

@Composable
fun ModeSelectionScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: ModeSelectionViewModel = viewModel(
        factory = ModeSelectionViewModelFactory(context)
    )

    val userSession by viewModel.userSession.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Delay UI rendering until loading is complete and user session is checked
    val shouldShowModeSelection = !isLoading && userSession.uuid == null

    // Handle navigation states
    var shouldNavigate by remember { mutableStateOf(false) }
    var destination by remember { mutableStateOf("") }

    // Perform initial navigation if user session exists
    LaunchedEffect(userSession.uuid, userSession.mode) {
        if (userSession.uuid != null && userSession.mode != null) {
            when (userSession.mode) {
                "surveillance" -> {
                    Toast.makeText(context, "Already saved as Surveillance", Toast.LENGTH_SHORT).show()
                    destination = "surveillance/${userSession.uuid}/surveillance"
                }
                "overlooker" -> {
                    destination = if (!userSession.connectedSurveillanceUUID.isNullOrBlank()) {
                        Toast.makeText(context, "Already connected as Overlooker", Toast.LENGTH_SHORT).show()
                        "overlooker_home/${userSession.uuid}/${userSession.connectedSurveillanceUUID}"
                    } else {
                        Toast.makeText(context, "Already saved as Overlooker", Toast.LENGTH_SHORT).show()
                        "overlooker_pair/${userSession.uuid}/overlooker"
                    }
                }
            }
            shouldNavigate = true
        }
    }

    if (isLoading) {
        AppLoadingScreen()
    } else if (shouldShowModeSelection) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome!",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Please select your device mode to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            ModeButton("Surveillance Mode") {
                val uuid = UUID.randomUUID().toString()
                viewModel.setMode("surveillance", uuid)
                destination = "surveillance/$uuid/surveillance"
                shouldNavigate = true
            }

            Spacer(modifier = Modifier.height(16.dp))

            ModeButton("Overlooker Mode") {
                val uuid = UUID.randomUUID().toString()
                viewModel.setMode("overlooker", uuid)
                destination = "overlooker_pair/$uuid/overlooker"
                shouldNavigate = true
            }
        }
    }

    // Use NavigateWithPermissionAndLoading for permission handling and navigation
    NavigateWithPermissionAndLoading(
        shouldNavigate = shouldNavigate,
        onNavigated = { shouldNavigate = false },
        destination = destination,
        navController = navController
    )
}

@Composable
fun ModeButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth(0.8f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
    }
}