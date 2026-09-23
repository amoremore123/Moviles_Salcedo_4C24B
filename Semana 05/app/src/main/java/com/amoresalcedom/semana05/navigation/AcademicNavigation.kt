package com.amoresalcedom.semana05.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.amoresalcedom.semana05.ui.AcademicDetailScreen
import com.amoresalcedom.semana05.ui.AcademicHomeScreen
import com.amoresalcedom.semana05.ui.ListScreen
import com.amoresalcedom.semana05.ui.LoginScreen
import com.amoresalcedom.semana05.ui.ProfileScreen

@Composable
fun AcademicNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            AcademicHomeScreen(
                onShowList = { navController.navigate(Screen.List.route) },
                onShowProfile = { navController.navigate(Screen.Profile.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
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
                onBackToHome = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("classId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("classId") ?: 0
            AcademicDetailScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
