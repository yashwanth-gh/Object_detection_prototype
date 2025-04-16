package com.example.objectdetectionapp.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.objectdetectionapp.ui.components.AppLoadingScreen
import com.example.objectdetectionapp.ui.overlooker.OverlookerHomeScreen
import com.example.objectdetectionapp.ui.overlooker.OverlookerPairScreen
import com.example.objectdetectionapp.ui.shared.ModeSelectionScreen
import com.example.objectdetectionapp.ui.shared.NavigationStateHandler
import com.example.objectdetectionapp.ui.surveillance.SurveillanceScreen

@Composable
fun NavGraph(navController: NavHostController) {
    val isNavigating by NavigationStateHandler.isNavigating.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
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
        if (isNavigating) {
            AppLoadingScreen(message = "Navigating...")
        }

    }
}