package com.lmstaskmanager.app.repository

import com.lmstaskmanager.app.model.*

object TaskRepository {

    val courses = listOf(
        Course("1", "Mathematics", "#3b82f6", "Dr. Smith"),
        Course("2", "Physics", "#8b5cf6", "Prof. Johnson"),
        Course("3", "Chemistry", "#ec4899", "Dr. Williams"),
        Course("4", "English Literature", "#10b981", "Ms. Brown"),
        Course("5", "Computer Science", "#06b6d4", "Dr. Wilson")
    )

    val tasks = mutableListOf(
        Task("1", "Review calculus notes", TaskStatus.TODO, "1"),
        Task("2", "Complete physics experiment", TaskStatus.DOING, "2"),
        Task("3", "Study chemistry formulas", TaskStatus.TODO, "3"),
        Task("4", "Read Chapter 5", TaskStatus.DONE, "4"),
        Task("5", "Debug code assignment", TaskStatus.TODO, "5")
    )

    val assignments = mutableListOf(
        Assignment("1", "Calculus Problem Set 5", "1", System.currentTimeMillis() + 172800000),
        Assignment("2", "Physics Lab Report", "2", System.currentTimeMillis() + 432000000),
        Assignment("3", "Chemistry Quiz", "3", System.currentTimeMillis() + 259200000),
        Assignment("4", "Python Project", "5", System.currentTimeMillis() + 518400000)
    )
    val scheduleEvents = listOf(
        ScheduleEvent("1", "Mathematics", 1, "09:00", "10:30", "1"),
        ScheduleEvent("2", "Physics", 1, "11:00", "12:30", "2"),
        ScheduleEvent("3", "Chemistry", 2, "09:00", "10:30", "3"),
        ScheduleEvent("4", "English Literature", 2, "11:00", "12:30", "4"),
        ScheduleEvent("5", "Computer Science", 3, "11:00", "12:30", "5"),
        ScheduleEvent("6", "Mathematics", 4, "09:00", "10:30", "1"),
        ScheduleEvent("7", "Physics Lab", 4, "13:00", "15:00", "2")
    )

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        tasks.indexOfFirst { it.id == taskId }
            .takeIf { it >= 0 }
            ?.let { tasks[it] = tasks[it].copy(status = newStatus) }
    }
}