package com.example.objectdetectionapp.ui.overlooker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OverlookerHomeScreen(
    overlookerUUID: String,
    surveillanceUUID: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Overlooker Home", modifier = Modifier.padding(bottom = 16.dp))
        Text(text = "Mode: Overlooker")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Overlooker UUID:\n$overlookerUUID")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Connected to Surveillance UUID:\n$surveillanceUUID")
    }
}