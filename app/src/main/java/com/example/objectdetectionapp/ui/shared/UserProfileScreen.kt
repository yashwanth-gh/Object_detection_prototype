package com.example.objectdetectionapp.ui.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.objectdetectionapp.R
import com.example.objectdetectionapp.ui.MainViewModel
import com.example.objectdetectionapp.ui.MainViewModelFactory

@Composable
fun UserProfileScreen() {
    val context = LocalContext.current
    val mainViewModel: MainViewModel = viewModel(factory = MainViewModelFactory(context))

    val scrollState = rememberScrollState()
    val currentMode by mainViewModel.userMode.collectAsState()
    val currentUuid by mainViewModel.userUUID.collectAsState()
    val connectedSurveillanceUUID by mainViewModel.connectedSurveillanceUUID.collectAsState()
    val userData by mainViewModel.userData.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 16.dp),
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Cursive
        )

        Spacer(modifier = Modifier.height(12.dp))

        Image(
            painter = painterResource(id = R.drawable.ic_user_image_round),
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(180.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        userData.let {
            Text(
                text = it.username ?: "Username",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )

            Text(
                text = it.email ?: "Email",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInfoRow("Mode", currentMode ?: "N/A")
                ProfileInfoRow("Your UUID", currentUuid ?: "N/A")

                if (currentMode == "overlooker") {
                    ProfileInfoRow("Connected Surveillance UUID", connectedSurveillanceUUID ?: "Not Connected")
                }
            }
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
