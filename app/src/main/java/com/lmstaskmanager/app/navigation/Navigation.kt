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
import com.lmstaskmanager.app.ui.theme.ScheduleScreen
import com.lmstaskmanager.app.ui.theme.AssignmentsScreen
import com.lmstaskmanager.app.ui.theme.CourseDetailScreen
import com.lmstaskmanager.app.ui.theme.SettingsScreen
import androidx.compose.runtime.getValue
import com.lmstaskmanager.app.settings.AppSettings
import com.lmstaskmanager.app.ui.theme.LoginScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Schedule : Screen("schedule")
    object Courses : Screen("courses")
    object Assignments : Screen("assignments")
    object CourseDetail : Screen("course/{courseId}") {
        fun createRoute(courseId: String) = "course/$courseId"
    }
    object Settings : Screen("settings")
}

@Composable
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current

    val startDestination = if (
        AppSettings.isLoggedIn(context) || AppSettings.hasSkippedLogin(context)
    ) Screen.Home.route else Screen.Login.route

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val noNavBarScreens = listOf(Screen.Login.route, Screen.Settings.route, Screen.CourseDetail.route)
            if (currentRoute !in noNavBarScreens) {
                BottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onConnectBlackboard = {
                        // OAuth flow — coming when API is approved
                    },
                    onContinueOffline = {
                        AppSettings.setSkippedLogin(context, true)
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(navController = navController)
            }
            composable(Screen.Assignments.route) {
                AssignmentsScreen()
            }
            composable(Screen.Schedule.route) {
                ScheduleScreen()
            }
            composable(Screen.Courses.route) {
                CoursesScreen(navController = navController)
            }
            composable(Screen.CourseDetail.route) { backStackEntry ->
                val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
                CourseDetailScreen(
                    courseId = courseId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}