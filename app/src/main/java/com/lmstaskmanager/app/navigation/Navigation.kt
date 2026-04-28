package com.lmstaskmanager.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Schedule : Screen("schedule")
    object Courses : Screen("courses")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            // HomeScreen() — coming soon
        }
        composable(Screen.Schedule.route) {
            // ScheduleScreen() — coming soon
        }
        composable(Screen.Courses.route) {
            // CoursesScreen() — coming soon
        }
    }
}