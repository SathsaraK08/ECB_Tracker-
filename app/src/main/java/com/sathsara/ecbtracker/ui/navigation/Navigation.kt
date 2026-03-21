package com.sathsara.ecbtracker.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sathsara.ecbtracker.R
import com.sathsara.ecbtracker.ui.screens.ForecastScreen
import com.sathsara.ecbtracker.ui.screens.HomeScreen
import com.sathsara.ecbtracker.ui.screens.LogScreen
import com.sathsara.ecbtracker.ui.screens.LoginScreen
import com.sathsara.ecbtracker.ui.screens.PaymentsScreen
import com.sathsara.ecbtracker.ui.screens.RecordsScreen
import com.sathsara.ecbtracker.ui.screens.ReportsScreen
import com.sathsara.ecbtracker.ui.screens.SettingsScreen
import com.sathsara.ecbtracker.ui.theme.Cyan
import com.sathsara.ecbtracker.ui.theme.Muted

@Composable
fun EcbNavHost(
    startDestination: String,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        // The main content area with Bottom Nav is wrapped in a composable 
        // that handles its own internal navigation for the 7 tabs.
        composable(Screen.Home.route) {
            MainScreenWithBottomNav {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) // Clear everything on logout
                }
            }
        }
    }
}

@Composable
fun MainScreenWithBottomNav(onLogout: () -> Unit) {
    val bottomNavController = rememberNavController()
    
    val navItems = listOf(
        Pair(Screen.Home, R.drawable.ic_home),
        Pair(Screen.Log, R.drawable.ic_add),
        Pair(Screen.Records, R.drawable.ic_list),
        Pair(Screen.Payments, R.drawable.ic_payment),
        Pair(Screen.Reports, R.drawable.ic_report),
        Pair(Screen.Forecast, R.drawable.ic_forecast),
        Pair(Screen.Settings, R.drawable.ic_settings)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = Muted
            ) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navItems.forEach { (screen, iconRes) ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = screen.route
                            )
                        },
                        selected = selected,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Cyan,
                            unselectedIconColor = Muted,
                            indicatorColor = MaterialTheme.colorScheme.surface // Hide pill bg matching mockup
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = bottomNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) { HomeScreen(navController = bottomNavController) }
            composable(Screen.Log.route) { LogScreen(navController = bottomNavController) }
            composable(Screen.Records.route) { RecordsScreen() }
            composable(Screen.Payments.route) { PaymentsScreen() }
            composable(Screen.Reports.route) { ReportsScreen() }
            composable(Screen.Forecast.route) { ForecastScreen() }
            composable(Screen.Settings.route) { SettingsScreen(onLogout = onLogout) }
        }
    }
}
