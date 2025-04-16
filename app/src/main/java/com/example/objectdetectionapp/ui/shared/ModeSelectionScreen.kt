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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun ModeSelectionScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: ModeSelectionViewModel = viewModel(
        factory = ModeSelectionViewModelFactory(context)
    )
    val scope = rememberCoroutineScope()

    val userSession by viewModel.userSession.collectAsState()


    val isLoading = userSession.mode == null && userSession.uuid == null


    // If mode is already selected, navigate directly
    LaunchedEffect(userSession) {
        val mode = userSession.mode
        val uuid = userSession.uuid
        val connectedSurveillanceUUID = userSession.connectedSurveillanceUUID
        if (!uuid.isNullOrBlank()) {
            when (mode) {
                "surveillance" -> {
                    Toast.makeText(context, "Already saved as Surveillance", Toast.LENGTH_SHORT).show()
                    navController.navigate("surveillance/${uuid}/surveillance") {
                        popUpTo("mode_selection") { inclusive = true }
                    }
                }

                "overlooker" -> {
                    if (!connectedSurveillanceUUID.isNullOrBlank()) {
                        Toast.makeText(context, "Already connected as Overlooker", Toast.LENGTH_SHORT).show()
                        navController.navigate("overlooker_home/${uuid}/${connectedSurveillanceUUID}") {
                            popUpTo("mode_selection") { inclusive = true }
                        }
                    } else {
                        Toast.makeText(context, "Already saved as Overlooker", Toast.LENGTH_SHORT).show()
                        navController.navigate("overlooker_pair/${uuid}/overlooker") {
                            popUpTo("mode_selection") { inclusive = true }
                        }
                    }
                }
            }
        }
    }

    // If no mode is saved, show selection screen
    if (userSession.mode == null || userSession.uuid == null) {
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

            ModeButton(
                label = "Surveillance Mode",
                onClick = {
                    scope.launch {
                        val uuid = UUID.randomUUID().toString()
                        viewModel.setMode("surveillance", uuid)
                        navController.navigate("surveillance/$uuid/surveillance") {
                            popUpTo("mode_selection") { inclusive = true }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ModeButton(
                label = "Overlooker Mode",
                onClick = {
                    scope.launch {
                        val uuid = UUID.randomUUID().toString()
                        viewModel.setMode("overlooker", uuid)
                        navController.navigate("overlooker_pair/$uuid/overlooker") {
                            popUpTo("mode_selection") { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ModeButton(
    label: String,
    onClick: () -> Unit
) {
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