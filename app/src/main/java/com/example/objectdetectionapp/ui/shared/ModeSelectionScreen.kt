package com.example.objectdetectionapp.ui.shared

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.objectdetectionapp.data.models.UserSessionData
import com.example.objectdetectionapp.ui.components.AppLoadingScreen
import kotlinx.coroutines.delay
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

    var isInitialized by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()

    // ⛔ Prevent premature composition by delaying UI rendering
    val shouldShowModeSelection = !isLoading &&
            !isInitialized &&
            userSession.mode == null &&
            userSession.uuid == null

    LaunchedEffect(userSession.uuid, userSession.mode) {
        NavigationStateHandler.stopNavigation()
        if (!isInitialized && !userSession.uuid.isNullOrBlank()) {
            isInitialized = true
            handleInitialNavigation(
                userSession = userSession,
                navController = navController,
                context = context
            )
        }
    }

    when {
        isLoading || (!isInitialized && userSession.uuid != null && userSession.mode != null) -> {
            AppLoadingScreen()
        }

        shouldShowModeSelection -> {
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
                            NavigationStateHandler.startNavigation()
                            navController.navigate("surveillance/$uuid/surveillance") {
                                popUpTo("mode_selection") { inclusive = true }
                            }
                            NavigationStateHandler.stopNavigation()
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
                            NavigationStateHandler.startNavigation()
                            navController.navigate("overlooker_pair/$uuid/overlooker") {
                                popUpTo("mode_selection") { inclusive = true }
                            }
                            NavigationStateHandler.stopNavigation()
                        }
                    }
                )
            }
        }

        else -> {
            // Safety fallback to avoid rendering ModeSelection briefly
            AppLoadingScreen()
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


// 3. Extracted Navigation Logic
private fun handleInitialNavigation(
    userSession: UserSessionData,
    navController: NavController,
    context: android.content.Context
) {
    val mode = userSession.mode
    val uuid = userSession.uuid
    val connectedSurveillanceUUID = userSession.connectedSurveillanceUUID

    when (mode) {
        "surveillance" -> {
            Toast.makeText(context, "Already saved as Surveillance", Toast.LENGTH_SHORT).show()
            navController.navigate("surveillance/$uuid/surveillance") {
                popUpTo("mode_selection") { inclusive = true }
            }
        }

        "overlooker" -> {
            if (!connectedSurveillanceUUID.isNullOrBlank()) {
                NavigationStateHandler.startNavigation()
                Toast.makeText(context, "Already connected as Overlooker", Toast.LENGTH_SHORT)
                    .show()
                navController.navigate("overlooker_home/$uuid/$connectedSurveillanceUUID") {
                    popUpTo("mode_selection") { inclusive = true }
                }
                NavigationStateHandler.stopNavigation()
            } else {
                NavigationStateHandler.startNavigation()
                Toast.makeText(context, "Already saved as Overlooker", Toast.LENGTH_SHORT).show()
                navController.navigate("overlooker_pair/$uuid/overlooker") {
                    popUpTo("mode_selection") { inclusive = true }
                }
                NavigationStateHandler.stopNavigation()
            }
        }
    }
}
