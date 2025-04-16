package com.example.objectdetectionapp.ui.overlooker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.objectdetectionapp.ui.shared.NavigationStateHandler

@Composable
fun OverlookerHomeScreen(
    overlookerUUID: String,
    surveillanceUUID: String
) {

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
            text = "Overlooker dashboard",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "status: connected",
            fontSize = 16.sp,
            color = Color(0xFF4CAF50), // greenish for success
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "this device's ID",
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = overlookerUUID,
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "connected to surveillance ID",
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = surveillanceUUID,
            fontSize = 13.sp,
            color = Color.Gray
        )
    }
}