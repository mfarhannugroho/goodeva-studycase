package com.goodeva.bletracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.goodeva.bletracker.presentation.history.HistoryScreen
import com.goodeva.bletracker.presentation.radar.RadarScreen
import com.goodeva.bletracker.presentation.scanner.ScannerScreen

sealed class Screen(val route: String) {
    data object Scanner : Screen("scanner")
    data object History : Screen("history")
    data object Radar : Screen("radar/{macAddress}") {
        fun createRoute(macAddress: String) = "radar/$macAddress"
    }
}

@Composable
fun BleTrackerNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Scanner.route
    ) {
        composable(Screen.Scanner.route) {
            ScannerScreen(
                onNavigateToRadar = { mac ->
                    navController.navigate(Screen.Radar.createRoute(mac))
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                }
            )
        }

        composable(
            route = Screen.Radar.route,
            arguments = listOf(
                navArgument("macAddress") { type = NavType.StringType }
            )
        ) {
            RadarScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRadar = { mac ->
                    navController.navigate(Screen.Radar.createRoute(mac))
                }
            )
        }
    }
}
