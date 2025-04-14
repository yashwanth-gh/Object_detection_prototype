package com.example.objectdetectionapp.ui.overlooker

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.objectdetectionapp.data.firebase.FirebaseServiceImpl
import com.example.objectdetectionapp.data.firebase.PushTokenManager
import com.example.objectdetectionapp.data.repository.UserPreferencesRepository
import com.example.objectdetectionapp.ui.shared.ModeSelectionViewModel
import com.example.objectdetectionapp.ui.shared.ModeSelectionViewModelFactory


@Composable
fun OverlookerPairScreen(
    uuid: String?,        // Overlooker UUID
    mode: String?,        // Mode = "overlooker"
    navController: NavController
) {
    val context = LocalContext.current

    // Instantiate ViewModel with factory
    val viewModel: OverlookerPairViewModel = viewModel(
        factory = OverlookerPairViewModelFactory(
            firebaseService = FirebaseServiceImpl(),
            userPreferencesRepository = UserPreferencesRepository(context, FirebaseServiceImpl()),
            overlookerUUID = uuid ?: ""
        )
    )

    var surveillanceUUID by remember { mutableStateOf("") }
    val pairingState by viewModel.pairingState.collectAsState()

    // Handle result feedback
    LaunchedEffect(pairingState,surveillanceUUID) {
        when (pairingState) {
            is OverlookerPairViewModel.PairingState.Success -> {
                Toast.makeText(context, "Connected successfully!", Toast.LENGTH_SHORT).show()
                uuid?.let { PushTokenManager.saveTokenToDatabase(it) }
                // Navigate to next screen
                 navController.navigate("overlooker_home/${uuid}/${surveillanceUUID}")
            }

            is OverlookerPairViewModel.PairingState.Error -> {
                val error = (pairingState as OverlookerPairViewModel.PairingState.Error).message
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }

            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Mode: $mode", fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Your UUID: $uuid", fontSize = 16.sp)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = surveillanceUUID,
            onValueChange = { surveillanceUUID = it },
            label = { Text("Enter Surveillance UUID") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.pairWithSurveillanceDevice(surveillanceUUID.trim())
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = pairingState != OverlookerPairViewModel.PairingState.Loading
        ) {
            Text(
                text = when (pairingState) {
                    OverlookerPairViewModel.PairingState.Loading -> "Connecting..."
                    else -> "Connect"
                }
            )
        }
    }
}