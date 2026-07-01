package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.navigation.*
import com.example.ui.HydrateViewModel
import com.example.ui.components.BottomNavBar
import com.example.ui.screens.*
import com.example.utils.FirebaseHelper
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseHelper.initialize(applicationContext)
        com.example.utils.NotificationHelper.createNotificationChannel(applicationContext)
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: HydrateViewModel = viewModel(factory = HydrateViewModel.Factory(application))
                val navController = rememberNavController()
                val navBackStackEntry = navController.currentBackStackEntryAsState()
                
                // Determine if bottom bar should be shown
                val showBottomBar = when (navBackStackEntry.value?.destination?.route) {
                    "splash", "onboarding" -> false
                    else -> true
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavBar(navController = navController)
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier.padding(innerPadding).fillMaxSize()
                    ) {
                        composable("splash") {
                            SplashScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("onboarding") {
                            OnboardingScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("home") {
                            HomeScreen(viewModel = viewModel, onNavigateToControl = {
                                navController.navigate("control")
                            })
                        }
                        composable("control") {
                            ControlScreen(viewModel = viewModel)
                        }
                        composable("progress") {
                            ProgressScreen(navController = navController, viewModel = viewModel)
                        }
                        composable("settings") {
                            SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
