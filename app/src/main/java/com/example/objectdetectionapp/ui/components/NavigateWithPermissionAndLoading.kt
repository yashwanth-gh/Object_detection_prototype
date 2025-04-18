package com.example.objectdetectionapp.ui.components

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.NavController
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NavigateWithPermissionAndLoading(
    shouldNavigate: Boolean,
    onNavigated: () -> Unit,
    destination: String,
    navController: NavController,
    permissions: Array<String> = emptyArray(),
    loadingTimeMillis: Long = 1000L
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsResult ->
        val allGranted = permissionsResult.all { it.value }
        if (allGranted) {
            showDialog = true
            coroutineScope.launch {
                delay(loadingTimeMillis)
                showDialog = false
                navController.navigate(destination)
                onNavigated()
            }
        } else {
            Toast.makeText(context, "Required permissions not granted", Toast.LENGTH_SHORT).show()
            onNavigated()
        }
    }

    if (shouldNavigate) {
        val notGrantedPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissions.isNotEmpty() && notGrantedPermissions.isNotEmpty()) {
            LaunchedEffect(Unit) {
                permissionLauncher.launch(notGrantedPermissions.toTypedArray())
            }
        } else {
            showDialog = true
            LaunchedEffect(Unit) {
                delay(loadingTimeMillis)
                showDialog = false
                navController.navigate(destination)
                onNavigated()
            }
        }
    }

    if (showDialog) {
        LoadingDialog()
    }
}

@Composable
fun LoadingDialog() {
    Dialog(onDismissRequest = { }) {
        Surface(
            color = Color.Black.copy(alpha = 0.7f),
            contentColor = Color.White
        ) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = androidx.compose.ui.Modifier
                    .padding(24.dp)
            )
        }
    }
}