package com.lmstaskmanager.app

import android.app.Application
import com.lmstaskmanager.app.database.DatabaseManager
import com.lmstaskmanager.app.settings.AutoDeleteManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LMSApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            DatabaseManager.seedIfEmpty(this@LMSApplication)
            AutoDeleteManager.runCleanup(this@LMSApplication)
        }

    }

}