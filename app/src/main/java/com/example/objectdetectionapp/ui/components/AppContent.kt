package com.example.objectdetectionapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.objectdetectionapp.ui.MainViewModel
import com.example.objectdetectionapp.ui.MainViewModelFactory
import com.example.objectdetectionapp.ui.navigation.NavGraph
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent() {
    val context = LocalContext.current
    val mainViewModel: MainViewModel = viewModel(factory = MainViewModelFactory(context))
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val currentMode by mainViewModel.userMode.collectAsState()
    val currentUuid by mainViewModel.userUUID.collectAsState()
    val connectedSurveillanceUUID by mainViewModel.connectedSurveillanceUUID.collectAsState()
    val userData by mainViewModel.userData.collectAsState()

    val showUIElements = currentRoute != "intro"

    if (showUIElements) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                SidePanel(
                    mode = currentMode,
                    uuid = currentUuid,
                    connectedSurveillanceUUID = connectedSurveillanceUUID,
                    username = userData.username,
                    email = userData.email,
                    navController = navController,
                    onCloseDrawer = { scope.launch { drawerState.close() } }
                )
            },
            content = {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    "DetectCam",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Cursive
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Open navigation drawer")
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        NavGraph(navController = navController)
                    }
                }
            }
        )
    } else {
        Scaffold(
            topBar = {} // No top bar
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                NavGraph(navController = navController)
            }
        }
    }
}