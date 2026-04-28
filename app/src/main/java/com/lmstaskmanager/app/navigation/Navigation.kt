package com.lmstaskmanager.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.lmstaskmanager.app.ui.theme.BottomNavBar
import com.lmstaskmanager.app.ui.theme.CoursesScreen
import com.lmstaskmanager.app.ui.theme.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Schedule : Screen("schedule")
    object Courses : Screen("courses")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen()
            }
            composable(Screen.Schedule.route) {
                // ScheduleScreen() — coming soon
            }
            composable(Screen.Courses.route) {
                CoursesScreen()
            }
        }
    }
}