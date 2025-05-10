package com.example.objectdetectionapp.ui.surveillance

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.objectdetectionapp.data.models.Overlooker
import com.example.objectdetectionapp.data.repository.MainRepository
import com.example.objectdetectionapp.utils.Resource

@Composable
fun ManageDevicesScreen(uuid: String?) {
    if (uuid == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Surveillance UUID not found.")
        }
        return
    }

    val context = LocalContext.current
    val viewModel: ManageDevicesViewModel = viewModel(
        factory = ManageDevicesViewModelFactory(context, surveillanceUUID = uuid)
    )

    val state by viewModel.overlookers.collectAsState()

    when (state) {
        is Resource.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is Resource.Success -> {
            val list = (state as Resource.Success<List<Overlooker>>).data
            if (list.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No Overlookers paired yet.")
                }
            } else {
                Column(
                    modifier = Modifier
                        .padding(6.dp)
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        "Manage Connected devices",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Cursive
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider()

                    LazyColumn {
                        items(list) { overlooker ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                elevation = CardDefaults.cardElevation(6.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {

                                    Text(
                                        text = overlooker.username,
                                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                            .align(Alignment.CenterHorizontally),
                                        fontWeight = FontWeight.Bold,

                                    )

                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(4.dp))


                                    // Email
                                    Text(
                                        text = "Email",
                                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = overlooker.email,
                                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    // UUID
                                    Text(
                                        text = "UUID",
                                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = overlooker.uuid,
                                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium
                                    )

                                    // Delete button
                                    Button(
                                        onClick = { viewModel.deleteOverlooker(overlooker.uuid) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 16.dp),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                            containerColor = androidx.compose.ui.graphics.Color.Red
                                        )
                                    ) {
                                        androidx.compose.material.icons.Icons.Filled.Delete.let {
                                            androidx.compose.material3.Icon(
                                                imageVector = it,
                                                contentDescription = "Delete",
                                                tint = androidx.compose.ui.graphics.Color.White
                                            )
                                        }
                                        Text(
                                            text = "Delete",
                                            color = androidx.compose.ui.graphics.Color.White,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }

        is Resource.Error -> {
            val errorMessage = (state as Resource.Error).throwable.message ?: "Unknown error"
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error loading overlookers: $errorMessage")
            }
        }
    }
}

