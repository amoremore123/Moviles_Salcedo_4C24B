package com.amoresalcedom.semana05.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.amoresalcedom.semana05.ui.DetailScreen
import com.amoresalcedom.semana05.ui.HomeScreen
import com.amoresalcedom.semana05.ui.ListScreen
import com.amoresalcedom.semana05.ui.ProfileScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onShowList = { navController.navigate(Screen.List.route) },
                onShowProfile = { navController.navigate(Screen.Profile.route) }
            )
        }
        composable(Screen.List.route) {
            ListScreen(
                onBack = { navController.popBackStack() },
                onItemSelected = { itemId ->
                    navController.navigate(Screen.Detail.createRoute(itemId))
                }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onBackToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
            DetailScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
