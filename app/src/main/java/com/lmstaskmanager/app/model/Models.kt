package com.lmstaskmanager.app.model

data class Course(
    val id: String,
    val name: String,
    val color: String,
    val teacher: String
)

data class Task(
    val id: String,
    val title: String,
    var status: TaskStatus,
    val courseId: String,
    val source: DataSource = DataSource.LOCAL
)

data class Assignment(
    val id: String,
    val title: String,
    val courseId: String,
    val dueDate: Long,
    var completed: Boolean = false,
    val source: DataSource = DataSource.LOCAL
)

data class ScheduleEvent(
    val id: String,
    val title: String,
    val day: Int,
    val startTime: String,
    val endTime: String,
    val courseId: String? = null,
    val isCustom: Boolean = false
)

enum class TaskStatus { TODO, DOING, DONE }

enum class DataSource { LOCAL, BLACKBOARD, CANVAS }