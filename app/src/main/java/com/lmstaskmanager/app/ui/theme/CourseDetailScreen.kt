package com.lmstaskmanager.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.lmstaskmanager.app.database.AssignmentEntity
import com.lmstaskmanager.app.database.DatabaseManager
import com.lmstaskmanager.app.model.Course
import com.lmstaskmanager.app.model.Task
import com.lmstaskmanager.app.model.DataSource
import com.lmstaskmanager.app.model.TaskStatus
import com.lmstaskmanager.app.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CourseDetailScreen(
    courseId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val course = TaskRepository.courses.find { it.id == courseId }
    val courseColor = course?.color?.let { Color(it.toColorInt()) } ?: Gray300

    var tasks by remember { mutableStateOf(emptyList<Task>()) }
    var assignments by remember { mutableStateOf(emptyList<AssignmentEntity>()) }

    LaunchedEffect(courseId) {
        withContext(Dispatchers.IO) {
            val db = DatabaseManager.getDatabase(context)
            tasks = db.taskDao().getAllTasks()
                .filter { it.courseId == courseId }
                .map { entity ->
                    Task(
                        id = entity.id,
                        title = entity.title,
                        status = TaskStatus.valueOf(entity.status),
                        courseId = entity.courseId,
                        source = DataSource.valueOf(entity.source)
                    )
                }
            assignments = db.assignmentDao().getAllAssignments()
                .filter { it.courseId == courseId }
                .sortedBy { it.dueDate }
        }
    }

    val now = System.currentTimeMillis()

    Column(modifier = Modifier.fillMaxSize().background(Gray50)) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Blue600)
                .padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = White
                    )
                }
                Column {
                    Text(
                        text = course?.name ?: "Course",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = White
                    )
                    Text(
                        text = course?.teacher ?: "",
                        fontSize = 12.sp,
                        color = White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tasks section
            item {
                SectionHeader(title = "Tasks", color = Gray900)
            }

            if (tasks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Gray200, RoundedCornerShape(8.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No tasks for this course", fontSize = 13.sp, color = Gray300)
                    }
                }
            } else {
                items(tasks) { task ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(0.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(courseColor, CircleShape)
                            )
                            Text(
                                text = task.title,
                                fontSize = 14.sp,
                                color = Gray900,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = task.status.name,
                                fontSize = 11.sp,
                                color = Gray400
                            )
                        }
                    }
                }
            }

            // Assignments section
            item {
                SectionHeader(title = "Assignments", color = Gray900)
            }

            if (assignments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Gray200, RoundedCornerShape(8.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No assignments for this course", fontSize = 13.sp, color = Gray300)
                    }
                }
            } else {
                items(assignments) { assignment ->
                    val isOverdue = !assignment.completed && assignment.dueDate < now
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    val dueText = dateFormat.format(Date(assignment.dueDate))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(0.dp),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(36.dp)
                                    .background(
                                        if (isOverdue) Red500 else courseColor,
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = assignment.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (assignment.completed) Gray400 else Gray900,
                                    textDecoration = if (assignment.completed)
                                        TextDecoration.LineThrough else TextDecoration.None
                                )
                                Text(
                                    text = "Due $dueText",
                                    fontSize = 11.sp,
                                    color = if (isOverdue) Red500 else Gray400
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}