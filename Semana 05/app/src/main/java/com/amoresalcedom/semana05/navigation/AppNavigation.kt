package com.amoresalcedom.semana05.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.amoresalcedom.semana05.ui.ConfirmationScreen
import com.amoresalcedom.semana05.ui.DetailScreen
import com.amoresalcedom.semana05.ui.HomeScreen
import com.amoresalcedom.semana05.ui.LoginScreen
import com.amoresalcedom.semana05.ui.ProfileScreen
import com.amoresalcedom.semana05.ui.ReservationsScreen
import com.amoresalcedom.semana05.ui.RoutinesScreen
import com.amoresalcedom.semana05.ui.gymClasses

@Composable
fun AppNavigation() {
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
            HomeScreen(
                onNavigateToReservations = { navController.navigate(Screen.Reservations.route) },
                onNavigateToRoutines = { navController.navigate(Screen.Routines.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onClassSelected = { classId ->
                    navController.navigate(Screen.Detail.createRoute(classId))
                }
            )
        }

        composable(Screen.Reservations.route) {
            ReservationsScreen(
                onBackToHome = { navController.popBackStack() }
            )
        }

        composable(Screen.Routines.route) {
            RoutinesScreen(
                onBackToHome = { navController.popBackStack() }
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
            val classId = backStackEntry.arguments?.getInt("classId") ?: 0
            val gymClass = gymClasses.firstOrNull { it.id == classId }

            DetailScreen(
                gymClass = gymClass,
                onBack = { navController.popBackStack() },
                onReserve = { slot ->
                    navController.navigate(Screen.Confirmation.createRoute(classId, slot))
                }
            )
        }

        composable(
            route = Screen.Confirmation.route,
            arguments = listOf(
                navArgument("classId") { type = NavType.IntType },
                navArgument("slot") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getInt("classId") ?: 0
            val slot = backStackEntry.arguments?.getString("slot") ?: ""
            val gymClass = gymClasses.firstOrNull { it.id == classId }

            ConfirmationScreen(
                gymClass = gymClass,
                slot = slot,
                onConfirm = {
                    navController.navigate(Screen.Reservations.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
