package com.example.objectdetectionapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.objectdetectionapp.ui.shared.ModeSelectionScreen
import com.example.objectdetectionapp.ui.shared.ModeSelectionViewModel
import com.example.objectdetectionapp.ui.surveillance.SurveillanceScreen
import com.example.objectdetectionapp.ui.surveillance.SurveillanceViewModel
import com.example.objectdetectionapp.ui.theme.ObjectDetectionAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        composable("surveillance") {
            SurveillanceScreen(navController)
        }
    }
}