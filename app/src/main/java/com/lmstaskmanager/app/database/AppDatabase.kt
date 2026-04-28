package com.lmstaskmanager.app.database

import androidx.room.*
import com.lmstaskmanager.app.model.TaskStatus
import com.lmstaskmanager.app.model.DataSource

// --- Entities (database tables) ---

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val status: String,
    val courseId: String,
    val source: String
)

@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val courseId: String,
    val dueDate: Long,
    val completed: Boolean,
    val source: String
)

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: String,
    val teacher: String
)

@Entity(tableName = "schedule_events")
data class ScheduleEventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val day: Int,
    val startTime: String,
    val endTime: String,
    val courseId: String?,
    val isCustom: Boolean
)

// --- DAOs (Data Access Objects) ---

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<TaskEntity>

    @Upsert
    suspend fun upsertTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("UPDATE tasks SET status = :status WHERE id = :id")
    suspend fun updateTaskStatus(id: String, status: String)
}

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments")
    suspend fun getAllAssignments(): List<AssignmentEntity>

    @Upsert
    suspend fun upsertAssignment(assignment: AssignmentEntity)

    @Delete
    suspend fun deleteAssignment(assignment: AssignmentEntity)
}

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    suspend fun getAllCourses(): List<CourseEntity>

    @Upsert
    suspend fun upsertCourse(course: CourseEntity)
}

@Dao
interface ScheduleEventDao {
    @Query("SELECT * FROM schedule_events")
    suspend fun getAllEvents(): List<ScheduleEventEntity>

    @Upsert
    suspend fun upsertEvent(event: ScheduleEventEntity)

    @Delete
    suspend fun deleteEvent(event: ScheduleEventEntity)
}

// --- Database ---

@Database(
    entities = [
        TaskEntity::class,
        AssignmentEntity::class,
        CourseEntity::class,
        ScheduleEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun courseDao(): CourseDao
    abstract fun scheduleEventDao(): ScheduleEventDao
}