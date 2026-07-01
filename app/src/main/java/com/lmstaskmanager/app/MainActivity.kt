package com.lmstaskmanager.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.lmstaskmanager.app.navigation.AppNavigation
import com.lmstaskmanager.app.ui.theme.LMSTaskManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            LMSTaskManagerTheme {
                AppNavigation()
            }
        }
    }
}