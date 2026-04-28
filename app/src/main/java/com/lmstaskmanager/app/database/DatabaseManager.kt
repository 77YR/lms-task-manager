package com.lmstaskmanager.app.database

import android.content.Context
import androidx.room.Room
import com.lmstaskmanager.app.repository.TaskRepository

object DatabaseManager {

    private var db: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        if (db == null) {
            db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "lms_database"
            ).build()
        }
        return db!!
    }

    suspend fun seedIfEmpty(context: Context) {
        val database = getDatabase(context)
        val existingTasks = database.taskDao().getAllTasks()

        if (existingTasks.isEmpty()) {
            // Seed courses
            TaskRepository.courses.forEach { course ->
                database.courseDao().upsertCourse(
                    CourseEntity(
                        id = course.id,
                        name = course.name,
                        color = course.color,
                        teacher = course.teacher
                    )
                )
            }

            // Seed tasks
            TaskRepository.tasks.forEach { task ->
                database.taskDao().upsertTask(
                    TaskEntity(
                        id = task.id,
                        title = task.title,
                        status = task.status.name,
                        courseId = task.courseId,
                        source = task.source.name
                    )
                )
            }

            // Seed assignments
            TaskRepository.assignments.forEach { assignment ->
                database.assignmentDao().upsertAssignment(
                    AssignmentEntity(
                        id = assignment.id,
                        title = assignment.title,
                        courseId = assignment.courseId,
                        dueDate = assignment.dueDate,
                        completed = assignment.completed,
                        source = assignment.source.name
                    )
                )
            }

            // Seed schedule events
            TaskRepository.scheduleEvents.forEach { event ->
                database.scheduleEventDao().upsertEvent(
                    ScheduleEventEntity(
                        id = event.id,
                        title = event.title,
                        day = event.day,
                        startTime = event.startTime,
                        endTime = event.endTime,
                        courseId = event.courseId,
                        isCustom = event.isCustom
                    )
                )
            }
        }
    }
}