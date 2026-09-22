package com.amoresalcedom.semana05.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object List : Screen("list")
    data object Profile : Screen("profile")
    data object Detail : Screen("detail/{itemId}") {
        fun createRoute(itemId: Int): String = "detail/$itemId"
    }
}
