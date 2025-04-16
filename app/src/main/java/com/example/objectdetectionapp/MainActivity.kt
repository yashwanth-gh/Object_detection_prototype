package com.example.objectdetectionapp

import android.content.pm.PackageManager
import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.objectdetectionapp.ui.overlooker.OverlookerHomeScreen
import com.example.objectdetectionapp.ui.overlooker.OverlookerPairScreen
import com.example.objectdetectionapp.ui.shared.ModeSelectionScreen
import com.example.objectdetectionapp.ui.surveillance.SurveillanceScreen
import com.example.objectdetectionapp.ui.theme.ObjectDetectionAppTheme

class MainActivity : ComponentActivity() {
    private val REQUEST_NOTIFICATION_PERMISSION = 123
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission for Android 13 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
        setContent {
            ObjectDetectionAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigator()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions as Array<String>, grantResults)
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can proceed with sending notifications
            } else {
                // Permission denied, handle accordingly (e.g., show a message to the user)
            }
        }
    }
}

@Composable
fun AppNavigator() {
    val navController: NavHostController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "mode_selection"
    ) {
        composable("mode_selection") {
            ModeSelectionScreen(navController)
        }
        composable(
            route = "surveillance/{uuid}/{mode}",
            arguments = listOf(
                navArgument("uuid") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType }
            )
        ) {
            val uuid = it.arguments?.getString("uuid")
            val mode = it.arguments?.getString("mode")
            SurveillanceScreen(uuid = uuid, mode = mode)
        }

        composable(
            route = "overlooker_pair/{uuid}/{mode}",
            arguments = listOf(
                navArgument("uuid") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType }
            )
        ) {
            val uuid = it.arguments?.getString("uuid")
            val mode = it.arguments?.getString("mode")
            OverlookerPairScreen(overlookerUUID = uuid, mode = mode, navController)
        }

        composable("overlooker_home/{overlookerUUID}/{surveillanceUUID}") { backStackEntry ->
            val overlookerUUID = backStackEntry.arguments?.getString("overlookerUUID") ?: ""
            val surveillanceUUID = backStackEntry.arguments?.getString("surveillanceUUID") ?: ""

            OverlookerHomeScreen(
                overlookerUUID = overlookerUUID,
                surveillanceUUID = surveillanceUUID
            )
        }
    }
}