package com.example.objectdetectionapp.ui.userSignIn

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import com.example.objectdetectionapp.ui.components.AppLoadingScreen
import com.example.objectdetectionapp.R // ensure your drawable exists here

@Composable
fun IntroScreen(navController: NavHostController) {
    val context = LocalContext.current.applicationContext as android.app.Application
    val viewModel: IntroViewModel =
        ViewModelProvider(LocalContext.current as androidx.activity.ComponentActivity)[IntroViewModel::class.java]

    val username by viewModel.username
    val email by viewModel.email
    val isLoading by viewModel.isLoading.collectAsState()
    val signInState by viewModel.signInState.collectAsState()
    val initialCheckComplete by viewModel.initialCheckComplete.collectAsState()

    val isEmailValid = remember(email) {
        android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    LaunchedEffect(signInState, initialCheckComplete) {
        when (signInState) {
            IntroViewModel.SignInState.Success,
            IntroViewModel.SignInState.AlreadySignedIn -> {
                navController.navigate("mode_selection") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                }
            }

            else -> {}
        }
    }

    if (isLoading || !initialCheckComplete || signInState == IntroViewModel.SignInState.AlreadySignedIn) {
        AppLoadingScreen()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Welcome to DetectCam",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = FontFamily.Cursive,
                ),
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 12.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Image(
                painter = painterResource(id = R.drawable.welcome),
                contentDescription = "Intro Illustration",
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .aspectRatio(1f)
            )

            if (email.isNotEmpty() && !isEmailValid) {
                Text(
                    text = "Please enter a valid email address",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = username,
                onValueChange = viewModel::onUsernameChanged,
                label = { Text("Enter your name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = viewModel::onEmailChanged,
                label = { Text("Enter your email") },
                isError = email.isNotEmpty() && !isEmailValid,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Your name and email will be used to send notifications and alert mails.",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontStyle = FontStyle.Normal,
                    color = MaterialTheme.colorScheme.outline,
                    fontFamily = FontFamily.Serif
                ),
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = viewModel::saveUserDetails,
                    enabled = signInState != IntroViewModel.SignInState.Loading && isEmailValid,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Text(
                        text = if (signInState == IntroViewModel.SignInState.Loading) "Saving data..." else "Continue",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }

            if (signInState == IntroViewModel.SignInState.Loading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            if (signInState is IntroViewModel.SignInState.Error) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = (signInState as IntroViewModel.SignInState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
