package com.example.objectdetectionapp.ui.shared

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun ModeSelectionScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val viewModel: ModeSelectionViewModel = viewModel(
        factory = ModeSelectionViewModelFactory(context)
    )
    val scope = rememberCoroutineScope()

    val mode by viewModel.mode.collectAsState()

    // If mode is already selected, navigate directly
    LaunchedEffect(mode) {
        if (mode == "surveillance") {
            navController.navigate("surveillance") {
                Toast.makeText(context,"already saved",Toast.LENGTH_SHORT).show()
                popUpTo("mode_selection") { inclusive = true }
            }
        }
    }

    // If no mode is saved, show selection screen
    if (mode == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Select Mode")

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {

                scope.launch {
                viewModel.setMode("surveillance")
                navController.navigate("surveillance") {
                    popUpTo("mode_selection") { inclusive = true }
                }
                }
            }) {
                Text(text = "Surveillance Mode")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                viewModel.setMode("overlooker")
                // Navigation for Overlooker mode will be handled later
            }) {
                Text(text = "Overlooker Mode (Coming Soon)")
            }
        }
    }
}