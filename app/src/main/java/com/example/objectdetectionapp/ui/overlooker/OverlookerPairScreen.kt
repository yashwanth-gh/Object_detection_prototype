package com.example.objectdetectionapp.ui.overlooker

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.objectdetectionapp.R
import com.example.objectdetectionapp.ui.components.NavigateWithPermissionAndLoading
import kotlinx.coroutines.launch


@Composable
fun OverlookerPairScreen(
    overlookerUUID: String?,
    mode: String?,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: OverlookerPairViewModel = viewModel(
        factory = OverlookerPairViewModelFactory(
            context,
            overlookerUUID = overlookerUUID.orEmpty()
        )
    )

    var pairingCode by remember { mutableStateOf("") }
    val pairingState by viewModel.pairingState.collectAsState()
    val surveillanceUUID by viewModel.surveillanceUUID.collectAsState()
    var shouldNavigate by remember { mutableStateOf(false) }


    LaunchedEffect(pairingState) {
        when (pairingState) {
            is OverlookerPairViewModel.PairingState.Success -> {
                Log.d("OverlookerPairScreen", "Pairing succeeded.")
                if (overlookerUUID != null) {
                    Log.d("OverlookerPairScreen", "Notifying surveillance device...")
                    scope.launch {
                        viewModel.notifySurveillanceOfPairing(surveillanceUUID)
                    }
                }
                Toast.makeText(context, "Connected successfully!", Toast.LENGTH_SHORT).show()
                shouldNavigate = true // Trigger navigation
            }

            is OverlookerPairViewModel.PairingState.Error -> {
                val error = (pairingState as OverlookerPairViewModel.PairingState.Error).message
                Log.e("OverlookerPairScreen", "Pairing error: $error")
                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
            }

            else -> Unit
        }
    }

    NavigateWithPermissionAndLoading(
        shouldNavigate = shouldNavigate,
        onNavigated = { shouldNavigate = false }, // Reset navigation flag
        destination = "overlooker_home/${overlookerUUID}/${surveillanceUUID}",
        navController = navController
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Connect with Camera Device",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Cursive
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Image(
            painter = painterResource(id = R.drawable.device_pair),
            contentDescription = null,
            modifier = Modifier
                .height(180.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "To connect, follow the steps below:",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "How to Connect",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = """
            1. On the other device, select Camera Mode.
            2. It will display a 6-digit pairing code.
            3. Enter that code below to link this device.
        """.trimIndent(),
            style = MaterialTheme.typography.bodySmall.copy(
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = pairingCode,
            onValueChange = { pairingCode = it },
            label = { Text("Enter 6-digit Pairing Code") },
            modifier = Modifier
                .fillMaxWidth(),
            singleLine = true,
            isError = pairingCode.length != 6
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (pairingCode.length == 6) {
                    scope.launch {
                        viewModel.pairWithSurveillanceDevice(pairingCode.trim())
                    }
                } else {
                    Toast.makeText(context, "Invalid pairing code.", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ),
            enabled = pairingState != OverlookerPairViewModel.PairingState.Loading
        ) {
            if (pairingState == OverlookerPairViewModel.PairingState.Loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Connecting...")
            } else {
                Text("Connect", style = MaterialTheme.typography.labelLarge)
            }
        }
    }

}