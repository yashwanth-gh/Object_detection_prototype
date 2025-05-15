package com.example.objectdetectionapp.ui.overlooker

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.objectdetectionapp.R
import com.example.objectdetectionapp.ui.components.NavigateWithPermissionAndLoading
import com.example.objectdetectionapp.utils.Resource

@Composable
fun OverlookerHomeScreen(
    overlookerUUID: String,
    surveillanceUUID: String,
    navController: NavHostController
) {
    val context = LocalContext.current
    val viewModel: OverlookerHomeViewModel =
        viewModel(factory = OverlookerHomeViewModelFactory(context))

    var navigateToDetectionScreen by remember { mutableStateOf(false) }
    var shouldNavigateToModeSelection by remember { mutableStateOf(false) }

    val deviceDataResource by viewModel.deviceData.collectAsState(initial = Resource.Loading())
    val isOverlookerValidResource by viewModel.isOverlookerValid.collectAsState(initial = Resource.Loading())

    val nightMode by viewModel.nightMode.collectAsState()
    val startCameraRemotely by viewModel.startCameraRemotely.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.checkOverlookerValidity(overlookerUUID, surveillanceUUID)
        viewModel.listenForNightModeAndStartCameraChangesToUpdateTheSwitch(surveillanceUUID)
    }
    when (isOverlookerValidResource) {
        is Resource.Loading -> {
            // Optionally show a loading indicator while checking validity
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Text("Checking session...")
            }
            return@OverlookerHomeScreen // Prevent rendering the main screen yet
        }
        is Resource.Success -> {
            val isValid = (isOverlookerValidResource as Resource.Success).data
            if (!isValid) {
                LaunchedEffect(Unit) {
                    viewModel.clearLocalDataAndNavigateToModeSelection()
                    shouldNavigateToModeSelection = true
                }
            }
        }
        is Resource.Error -> {
            // Handle error during validity check (e.g., network issues)
            // You might want to show an error message or retry
            Log.e("OverlookerHomeScreen", "Error checking overlooker validity: ${(isOverlookerValidResource as Resource.Error).throwable.message}")
            // For now, let's assume valid and proceed (you might want different behavior)
        }
    }

    // Navigate back to mode selection if the overlooker is no longer valid
    if (shouldNavigateToModeSelection) {
        LaunchedEffect(Unit) {
            navController.navigate("mode_selection") {
                popUpTo("overlooker_home/{$overlookerUUID}/{$surveillanceUUID}") {
                    inclusive = true
                }
            }
        }
        return@OverlookerHomeScreen // Prevent rendering the main screen
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Monitoring Dashboard",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Cursive
        )

        Image(
            painter = painterResource(id = R.drawable.monitoring), // Use a custom drawable for Overlooker
            contentDescription = "Overlooker Illustration",
            modifier = Modifier.height(250.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        when (deviceDataResource) {
            is Resource.Loading -> {
                CircularProgressIndicator()
                Text("Fetching surveillance info...")
            }

            is Resource.Success -> {
                val deviceData = (deviceDataResource as Resource.Success).data

                // Extract Overlooker Info using UUID
                val overlookerInfo = deviceData.overlookers[overlookerUUID]

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("🌙 Vigilant Night Monitoring", modifier = Modifier.weight(1f),fontSize = 13.sp)
                    Switch(
                        checked = nightMode,
                        onCheckedChange = { isOn ->
                            viewModel.setNightMode(surveillanceUUID, isOn)
                        }
                    )
                }


                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎦 Turn Surveillance Camera ON/OFF", Modifier.weight(1f), fontSize = 13.sp)
                    Switch(
                        checked = startCameraRemotely,
                        onCheckedChange = { isOn ->
                            viewModel.setStartCamera(surveillanceUUID, isOn)
                        }
                    )
                }

                Button(
                    onClick = { navigateToDetectionScreen = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("See All Detections")
                }

                if (overlookerInfo != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "This Device Info:",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF388E3C))
                            ) {
                                Text(
                                    text = "active",
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Light,
                                    textAlign = TextAlign.Center,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = overlookerInfo.username,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = overlookerInfo.email,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(4.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Camera Mode Device Info:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF388E3C))
                        ) {
                            Text(
                                text = "connected",
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = deviceData.user.username,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = deviceData.user.email,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

            }

            is Resource.Error -> {
                val error = (deviceDataResource as Resource.Error).throwable.message
                Text("Error loading data: $error", color = Color.Red)
            }
        }

        NavigateWithPermissionAndLoading(
            shouldNavigate = navigateToDetectionScreen,
            onNavigated = { navigateToDetectionScreen = false },
            destination = "detections_screen",
            navController = navController
        )
    }
}

