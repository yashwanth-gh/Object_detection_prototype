package com.example.objectdetectionapp.ui.surveillance

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.objectdetectionapp.R
import com.example.objectdetectionapp.ui.components.NavigateWithPermissionAndLoading
import com.example.objectdetectionapp.utils.Resource

@Composable
fun SurveillanceScreen(
    uuid: String?,
    mode: String?,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: SurveillanceViewModel =
        viewModel(factory = SurveillanceViewModelFactory(context))

    val deviceDataResource by viewModel.deviceData.collectAsState(initial = Resource.Loading())

    var showPairingCode by remember { mutableStateOf(false) }
    var navigateToCamera by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            "Camera Mode Dashboard",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Cursive
        )

        Image(
            painter = painterResource(id = R.drawable.camera),
            contentDescription = "Camera Mode Illustration",
            modifier = Modifier.height(200.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))


        when (deviceDataResource) {
            is Resource.Loading -> {
                CircularProgressIndicator()
                Text("Loading Device Data...")
            }

            is Resource.Success -> {
                val deviceData = (deviceDataResource as Resource.Success).data

                Button(
                    onClick = { navigateToCamera = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Click to Start Detection")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .padding(top = 6.dp, bottom = 6.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "This device info :",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (deviceData.status == "active") Color(0xFF388E3C) else Color(
                                    0xFFFFEBEE
                                )
                            ),
                            modifier = Modifier
                                .wrapContentWidth()
                                .align(Alignment.CenterHorizontally) // Center align the Card itself
                        ) {
                            Text(
                                text = deviceData.status,
                                color = if (deviceData.status == "active") Color(0xFFE8F5E9) else Color.Red,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp),
                                fontWeight = FontWeight.Light,
                                textAlign = TextAlign.Center,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))

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

                        Spacer(modifier = Modifier.height(12.dp))

                        if (showPairingCode && uuid != null) {
                            Text(
                                text = uuid.take(6),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF00E5FF),
                                letterSpacing = 4.sp
                            )

                        }

                        Text(
                            text = if (showPairingCode) "hide pairing code" else "show pairing code",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable { showPairingCode = !showPairingCode }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                HorizontalDivider()

                // Connected Devices Section
                deviceData.overlookers.let { overlookers ->
                    if (overlookers.isEmpty()) {
                        Text(
                            text = "No Overlookers connected yet.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Connected Devices (${overlookers.size})",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(vertical = 6.dp),
                            )

                            LazyColumn {
                                items(overlookers.values.toList().withIndex().toList(), key = { it.index }) { (_, overlooker) ->
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = overlooker.username,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = overlooker.email,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider()

            }
            is Resource.Error -> {
                val error = (deviceDataResource as Resource.Error).throwable.message
                Text("Error loading device data: $error", color = Color.Red)
            }
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

