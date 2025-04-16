package com.example.objectdetectionapp.ui.surveillance

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.objectdetectionapp.ui.shared.NavigationStateHandler
import kotlinx.coroutines.launch

@Composable
fun SurveillanceScreen(
    uuid: String?,
    mode: String?
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: SurveillanceViewModel = viewModel(
        factory = SurveillanceViewModelFactory(context)
    )

    LaunchedEffect(Unit) {
        NavigationStateHandler.stopNavigation()
    }

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

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "this device's mode",
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = mode ?: "N/A",
            fontSize = 13.sp,
            color = Color.Gray
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

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                scope.launch {
                    uuid?.let {
                        viewModel.notifyOverlookers(surveillanceUUID = it)
                    }  }

            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)) // Stylish blue
        ) {
            Text("notify all connected devices")
        }
    }
}
