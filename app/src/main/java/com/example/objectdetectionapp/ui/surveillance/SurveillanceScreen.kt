package com.example.objectdetectionapp.ui.surveillance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.objectdetectionapp.ui.components.NavigateWithPermissionAndLoading
import kotlinx.coroutines.launch

@Composable
fun SurveillanceScreen(
    uuid: String?,
    mode: String?,
    navController: NavController
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: SurveillanceViewModel = viewModel(
        factory = SurveillanceViewModelFactory(context)
    )

    var showPairingCode by remember { mutableStateOf(false) }
    var navigateToCamera by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Surveillance dashboard",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Status: •Active",
            fontSize = 16.sp,
            color = Color(0xFF4CAF50), // greenish for success
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "this device's ID",
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = uuid ?: "N/A",
            fontSize = 13.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(10.dp))


        if (showPairingCode && uuid != null) {
            Text(
                text = uuid.take(6),
                fontSize = 28.sp, // Larger than typical title size
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (showPairingCode) "Hide Pairing Code" else "Show Pairing Code",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .clickable { showPairingCode = !showPairingCode }
        )

        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                scope.launch {
                    uuid?.let {
                        viewModel.notifyOverlookers(surveillanceUUID = it)
                    }
                }

            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)) // Stylish blue
        ) {
            Text("notify all connected devices")
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = {
                navigateToCamera = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)) // Green
        ) {
            Text("Start Surveillance")
        }

        NavigateWithPermissionAndLoading(
            shouldNavigate = navigateToCamera,
            onNavigated = { navigateToCamera = false },
            destination = "camera_preview_screen",
            navController = navController,
            permissions = arrayOf(android.Manifest.permission.CAMERA)
        )

    }
}
