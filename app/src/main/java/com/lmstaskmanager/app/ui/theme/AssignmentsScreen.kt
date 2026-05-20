package com.lmstaskmanager.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AssignmentsScreen() {
    val context = LocalContext.current
    var assignments by remember { mutableStateOf(emptyList<AssignmentEntity>()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val db = DatabaseManager.getDatabase(context)
            assignments = db.assignmentDao().getAllAssignments()
                .sortedBy { it.dueDate }
        }
    }

    val courses = com.lmstaskmanager.app.repository.TaskRepository.courses
    val now = System.currentTimeMillis()

    val overdue = assignments.filter { !it.completed && it.dueDate < now }
    val upcoming = assignments.filter { !it.completed && it.dueDate >= now }
    val completed = assignments.filter { it.completed }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Assignments", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        if (overdue.isNotEmpty()) {
            item {
                Text(
                    text = "Overdue",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE53935)
                )
            }
            items(overdue) { assignment ->
                AssignmentCard(
                    assignment = assignment,
                    courses = courses,
                    isOverdue = true,
                    onToggleComplete = { updated ->
                        assignments = assignments.map {
                            if (it.id == updated.id) updated else it
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            DatabaseManager.getDatabase(context)
                                .assignmentDao()
                                .upsertAssignment(updated)
                        }
                    }
                )
            }
        }

        if (upcoming.isNotEmpty()) {
            item {
                Text(
                    text = "Upcoming",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E88E5)
                )
            }
            items(upcoming) { assignment ->
                AssignmentCard(
                    assignment = assignment,
                    courses = courses,
                    isOverdue = false,
                    onToggleComplete = { updated ->
                        assignments = assignments.map {
                            if (it.id == updated.id) updated else it
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            DatabaseManager.getDatabase(context)
                                .assignmentDao()
                                .upsertAssignment(updated)
                        }
                    }
                )
            }
        }

        if (completed.isNotEmpty()) {
            item {
                Text(
                    text = "Completed",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
            }
            items(completed) { assignment ->
                AssignmentCard(
                    assignment = assignment,
                    courses = courses,
                    isOverdue = false,
                    onToggleComplete = { updated ->
                        assignments = assignments.map {
                            if (it.id == updated.id) updated else it
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            DatabaseManager.getDatabase(context)
                                .assignmentDao()
                                .upsertAssignment(updated)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AssignmentCard(
    assignment: AssignmentEntity,
    courses: List<com.lmstaskmanager.app.model.Course>,
    isOverdue: Boolean,
    onToggleComplete: (AssignmentEntity) -> Unit
) {
    val course = courses.find { it.id == assignment.courseId }
    val courseColor = course?.color?.let { Color(it.toColorInt()) } ?: Color.Gray
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dueText = dateFormat.format(Date(assignment.dueDate))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .background(
                        if (isOverdue) Color(0xFFE53935) else courseColor,
                        RoundedCornerShape(2.dp)
                    )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = assignment.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (assignment.completed)
                        TextDecoration.LineThrough else TextDecoration.None,
                    color = if (assignment.completed) Color.Gray else Color.Unspecified
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = course?.name ?: "No Course",
                    fontSize = 12.sp,
                    color = courseColor
                )
                Text(
                    text = "Due: $dueText",
                    fontSize = 11.sp,
                    color = if (isOverdue) Color(0xFFE53935) else Color.Gray
                )
            }
            IconButton(
                onClick = {
                    onToggleComplete(assignment.copy(completed = !assignment.completed))
                }
            ) {
                Icon(
                    imageVector = if (assignment.completed)
                        Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = "Toggle complete",
                    tint = if (assignment.completed) Color(0xFF43A047) else Color.Gray
                )
            }
        }
    }
}