package com.amoresalcedom.semana05.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Home : Screen("home")
    data object Reservations : Screen("reservations")
    data object Routines : Screen("routines")
    data object Profile : Screen("profile")
    data object Detail : Screen("detail/{classId}") {
        fun createRoute(classId: Int): String = "detail/$classId"
    }
    data object Confirmation : Screen("confirmation/{classId}/{slot}") {
        fun createRoute(classId: Int, slot: String): String = "confirmation/$classId/$slot"
    }
}
