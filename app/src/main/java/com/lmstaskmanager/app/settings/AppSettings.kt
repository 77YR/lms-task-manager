package com.lmstaskmanager.app.settings

import android.content.Context

object AppSettings {

    private const val PREFS_NAME = "lms_settings"
    private const val KEY_AUTO_DELETE_ENABLED = "auto_delete_enabled"
    private const val KEY_ASSIGNMENT_DELETE_DAYS = "assignment_delete_days"
    private const val KEY_TASK_DELETE_DAYS = "task_delete_days"

    fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isAutoDeleteEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_AUTO_DELETE_ENABLED, true)

    fun setAutoDeleteEnabled(context: Context, enabled: Boolean) =
        getPrefs(context).edit().putBoolean(KEY_AUTO_DELETE_ENABLED, enabled).apply()

    fun getAssignmentDeleteDays(context: Context): Int =
        getPrefs(context).getInt(KEY_ASSIGNMENT_DELETE_DAYS, 1)

    fun setAssignmentDeleteDays(context: Context, days: Int) =
        getPrefs(context).edit().putInt(KEY_ASSIGNMENT_DELETE_DAYS, days).apply()

    fun getTaskDeleteDays(context: Context): Int =
        getPrefs(context).getInt(KEY_TASK_DELETE_DAYS, 2)

    fun setTaskDeleteDays(context: Context, days: Int) =
        getPrefs(context).edit().putInt(KEY_TASK_DELETE_DAYS, days).apply()
    fun isLoggedIn(context: Context): Boolean =
        getPrefs(context).getBoolean("is_logged_in", false)

    fun setLoggedIn(context: Context, loggedIn: Boolean) =
        getPrefs(context).edit().putBoolean("is_logged_in", loggedIn).apply()

    fun setSkippedLogin(context: Context, skipped: Boolean) =
        getPrefs(context).edit().putBoolean("skipped_login", skipped).apply()

    fun hasSkippedLogin(context: Context): Boolean =
        getPrefs(context).getBoolean("skipped_login", false)
}