package com.example.objectdetectionapp.ui.surveillance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.objectdetectionapp.ui.shared.ModeSelectionViewModel
import com.example.objectdetectionapp.ui.shared.ModeSelectionViewModelFactory

@Composable
fun SurveillanceScreen(
    uuid: String?,
    mode: String?
) {

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Surveillance Mode")

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Your UUID: ${uuid ?: "N/A"}")
        Text(text = "Your mode: ${mode ?: "N/A"}")
    }
}
