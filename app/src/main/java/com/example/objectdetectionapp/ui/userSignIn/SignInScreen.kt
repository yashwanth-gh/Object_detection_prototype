package com.example.objectdetectionapp.ui.userSignIn

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import com.example.objectdetectionapp.ui.components.AppLoadingScreen

@Composable
fun SignInScreen(navController: NavHostController) {
    val context = LocalContext.current.applicationContext as android.app.Application
    val viewModel: SignInViewModel =
        ViewModelProvider(LocalContext.current as androidx.activity.ComponentActivity)[SignInViewModel::class.java]

    // Collect state flows
    val username by viewModel.username
    val email by viewModel.email
    val isLoading by viewModel.isLoading.collectAsState()
    val signInState by viewModel.signInState.collectAsState()
    val initialCheckComplete by viewModel.initialCheckComplete.collectAsState()

    // Handle navigation based on sign-in state
    LaunchedEffect(signInState, initialCheckComplete) {
        when (signInState) {
            SignInViewModel.SignInState.Success,
            SignInViewModel.SignInState.AlreadySignedIn -> {
                navController.navigate("mode_selection") {
                    // Clear the back stack to prevent returning to sign-in
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }
            }
            else -> { /* No action needed */ }
        }
    }

    // Always show loading screen until initial check completes and we know the user isn't already signed in
    if (isLoading || !initialCheckComplete || signInState == SignInViewModel.SignInState.AlreadySignedIn) {
        AppLoadingScreen()
    } else {
        // Only show sign-in form when not already signed in and initial check is complete
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Sign In",
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = viewModel::onUsernameChanged,
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = viewModel::onEmailChanged,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = viewModel::saveUserDetails,
                enabled = signInState != SignInViewModel.SignInState.Loading
            ) {
                Text(
                    text = if (signInState == SignInViewModel.SignInState.Loading) "Signing In..." else "Continue"
                )
            }

            if (signInState == SignInViewModel.SignInState.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            // Show error message if any
            if (signInState is SignInViewModel.SignInState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (signInState as SignInViewModel.SignInState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}