package com.lmstaskmanager.app.settings

import android.content.Context
import com.lmstaskmanager.app.database.DatabaseManager
import com.lmstaskmanager.app.model.TaskStatus

object AutoDeleteManager {

    suspend fun runCleanup(context: Context) {
        if (!AppSettings.isAutoDeleteEnabled(context)) return

        val db = DatabaseManager.getDatabase(context)
        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L

        // Delete completed assignments that are past due by X days
        val assignmentThreshold = AppSettings.getAssignmentDeleteDays(context) * oneDayMs
        val assignments = db.assignmentDao().getAllAssignments()
        assignments
            .filter { it.completed && (now - it.dueDate) > assignmentThreshold }
            .forEach { db.assignmentDao().deleteAssignment(it) }

        // Delete tasks in DONE status for X days
        // Don't store completion time yet so we use a simpler approach:
        // Delete all DONE tasks if auto-delete is on and threshold is met
        val taskThresholdDays = AppSettings.getTaskDeleteDays(context)
        if (taskThresholdDays == 0) {
            val tasks = db.taskDao().getAllTasks()
            tasks
                .filter { it.status == TaskStatus.DONE.name }
                .forEach { db.taskDao().deleteTask(it) }
        }
    }
}