package com.michael.frozendroid.presentation.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun FrozenDroidNavHost(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            val hViewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = hViewModel,
                onNavigateToProfiles = { navController.navigate("profiles") },
                onNavigateToCpu = { navController.navigate("cpu") },
                onNavigateToSafeDirectory = { navController.navigate("directory") }
            )
        }
        composable("profiles") {
            val pViewModel: ProfilesViewModel = hiltViewModel()
            ProfilesScreen(
                viewModel = pViewModel,
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToCpu = { navController.navigate("cpu") },
                onNavigateToSafeDirectory = { navController.navigate("directory") }
            )
        }
        composable("cpu") {
            val cViewModel: CpuMonitorViewModel = hiltViewModel()
            CpuMonitorScreen(
                viewModel = cViewModel,
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToProfiles = { navController.navigate("profiles") },
                onNavigateToSafeDirectory = { navController.navigate("directory") }
            )
        }
        composable("directory") {
            val dViewModel: SafeDirectoryViewModel = hiltViewModel()
            SafeDirectoryScreen(
                viewModel = dViewModel,
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToProfiles = { navController.navigate("profiles") },
                onNavigateToCpu = { navController.navigate("cpu") }
            )
        }
    }
}
