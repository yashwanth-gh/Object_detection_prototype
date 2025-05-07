package com.example.objectdetectionapp.ui.shared

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.res.painterResource
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.objectdetectionapp.R
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
    val modeSelectionState by viewModel.modeSelectionState.collectAsState()

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

    // Handle the state of mode selection and saving
    LaunchedEffect(modeSelectionState) {
        when (modeSelectionState) {
            is ModeSelectionViewModel.ModeSelectionState.Success -> {
                shouldNavigate = true
            }
            is ModeSelectionViewModel.ModeSelectionState.Error -> {
                val errorMessage = (modeSelectionState as ModeSelectionViewModel.ModeSelectionState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                shouldNavigate = false
                destination = ""
            }
            ModeSelectionViewModel.ModeSelectionState.Loading -> {
                // Optionally, show a loading indicator here if needed during the save operation
            }
            ModeSelectionViewModel.ModeSelectionState.Idle -> {
                // Initial state, no action needed
            }
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
                text = "Select App Mode",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(id = R.drawable.mode_trans),
                contentDescription = "Device Mode Illustration",
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ModeButton(
                label = if (modeSelectionState == ModeSelectionViewModel.ModeSelectionState.Loading) "Connecting..." else "Camera Mode",
                enabled = modeSelectionState != ModeSelectionViewModel.ModeSelectionState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shadow(4.dp)
            ) {
                val uuid = UUID.randomUUID().toString()
                viewModel.setMode("surveillance", uuid)
                destination = "surveillance/$uuid/surveillance"
            }

            Spacer(modifier = Modifier.height(16.dp))

            ModeButton(
                label = if (modeSelectionState == ModeSelectionViewModel.ModeSelectionState.Loading) "Connecting..." else "Monitor Mode",
                enabled = modeSelectionState != ModeSelectionViewModel.ModeSelectionState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shadow(4.dp)
            ) {
                val uuid = UUID.randomUUID().toString()
                viewModel.setMode("overlooker", uuid)
                destination = "overlooker_pair/$uuid/overlooker"
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                            append("👉 Camera Mode: ")
                        }
                        append("Use this device to detect people and send alerts.")
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.outline,
                        fontStyle = FontStyle.Italic
                    ),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                            append("👉 Monitor Mode: ")
                        }
                        append("Use this device to receive notifications and stay updated.")
                    },
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.outline,
                        fontStyle = FontStyle.Italic
                    ),
                    textAlign = TextAlign.Start
                )
            }

            if (modeSelectionState == ModeSelectionViewModel.ModeSelectionState.Loading) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
fun ModeButton(
    label: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        shape = MaterialTheme.shapes.medium,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 10.dp,
            disabledElevation = 0.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp)
        )
    }
}
