package com.example.objectdetectionapp.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.objectdetectionapp.ui.overlooker.OverlookerHomeScreen
import com.example.objectdetectionapp.ui.overlooker.OverlookerPairScreen
import com.example.objectdetectionapp.ui.shared.DetectionScreen
import com.example.objectdetectionapp.ui.shared.ModeSelectionScreen
import com.example.objectdetectionapp.ui.surveillance.CameraPreviewScreen
import com.example.objectdetectionapp.ui.surveillance.SurveillanceScreen
import com.example.objectdetectionapp.ui.userSignIn.SignInScreen
import com.example.objectdetectionapp.ui.userSignIn.SignInViewModel

@Composable
fun NavGraph(navController: NavHostController) {

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "sign_in" // Initial start destination
        ) {
            composable("sign_in") {
                SignInScreen(navController = navController)
            }
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
                SurveillanceScreen(uuid = uuid, mode = mode, navController)
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

            composable("camera_preview_screen") {
                CameraPreviewScreen()
            }

            composable("detections_screen") {
                DetectionScreen()
            }
        }
    }
}