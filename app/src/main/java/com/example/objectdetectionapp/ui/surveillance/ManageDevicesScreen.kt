package com.example.objectdetectionapp.ui.surveillance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.objectdetectionapp.data.models.Overlooker
import com.example.objectdetectionapp.data.repository.MainRepository
import com.example.objectdetectionapp.utils.Resource

@Composable
fun ManageDevicesScreen(uuid: String?) {
    if (uuid == null) {
        Text("Surveillance UUID not found.")
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
                Text("No Overlookers paired yet.")
            } else {
                LazyColumn {
                    items(list) { overlooker ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("UUID: ${overlooker.uuid}")
                                Text("Email: ${overlooker.email}")
                                Text("Username: ${overlooker.username}")

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(onClick = {
                                        viewModel.deleteOverlooker(overlooker.uuid)
                                    }) {
                                        Text("Delete")
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
            Text("Error loading overlookers: $errorMessage")
        }
    }

}

