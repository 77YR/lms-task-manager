package com.lmstaskmanager.app.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lmstaskmanager.app.navigation.Screen
import androidx.compose.material.icons.filled.Settings

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        Triple(Screen.Home, Icons.Filled.Home, "Home"),
        Triple(Screen.Assignments, Icons.AutoMirrored.Filled.Assignment, "Assignments"),
        Triple(Screen.Schedule, Icons.Filled.CalendarMonth, "Schedule"),
        Triple(Screen.Courses, Icons.Filled.School, "Courses")
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { (screen, icon, label) ->
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}