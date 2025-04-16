package com.example.objectdetectionapp.ui.overlooker

import android.util.Log
import android.widget.Toast
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.objectdetectionapp.data.firebase.FirebaseServiceImpl
import com.example.objectdetectionapp.data.repository.UserPreferencesRepository
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
            firebaseService = FirebaseServiceImpl(),
            userPreferencesRepository = UserPreferencesRepository(context, FirebaseServiceImpl()),
            overlookerUUID = overlookerUUID.orEmpty()
        )
    )

    var surveillanceUUID by remember { mutableStateOf("") }
    val pairingState by viewModel.pairingState.collectAsState()

    LaunchedEffect(pairingState, surveillanceUUID, overlookerUUID) {
        when (pairingState) {
            is OverlookerPairViewModel.PairingState.Success -> {

                Log.d("OverlookerPairScreen", "Pairing succeeded. Proceeding to next steps.")


                if (overlookerUUID != null) {
                    Log.d(
                        "OverlookerPairScreen",
                        "attempting to send notification to surveillance device"
                    )
                    scope.launch {
                        viewModel.notifySurveillanceOfPairing(
                            surveillanceUUID
                        )
                    }
                }

                Toast.makeText(context, "Connected successfully!", Toast.LENGTH_SHORT).show()
                navController.navigate("overlooker_home/${overlookerUUID}/${surveillanceUUID}")
            }

            is OverlookerPairViewModel.PairingState.Error -> {
                val error = (pairingState as OverlookerPairViewModel.PairingState.Error).message
                Log.e("OverlookerPairScreen", "Pairing error: $error")
                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
            }

            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Connect to SURVEILLANCE DEVICE",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "to connect, follow the steps below:",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "where to find id",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = """
        1. on the other device, select surveillance mode.
        2. after selecting, it will display an id on the screen.
        3. enter that id below to link this device as an overlooker.
    """.trimIndent(),
            fontSize = 14.sp,
            color = Color.Gray,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "this device's id: $overlookerUUID",
            fontSize = 13.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = surveillanceUUID,
            onValueChange = { surveillanceUUID = it },
            label = { Text("Surveillance ID") },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch { viewModel.pairWithSurveillanceDevice(surveillanceUUID.trim())  }

            },
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth(0.8f),
            shape = RoundedCornerShape(10.dp),
            enabled = pairingState != OverlookerPairViewModel.PairingState.Loading
        ) {
            if (pairingState == OverlookerPairViewModel.PairingState.Loading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp,
                    modifier = Modifier
                        .size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Connecting...")
            } else {
                Text("Connect")
            }
        }
    }
}