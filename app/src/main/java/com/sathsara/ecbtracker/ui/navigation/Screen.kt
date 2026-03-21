package com.sathsara.ecbtracker.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Log : Screen("log")
    object Records : Screen("records")
    object Payments : Screen("payments")
    object Reports : Screen("reports")
    object Forecast : Screen("forecast")
    object Settings : Screen("settings")
}
