package com.lmstaskmanager.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.lmstaskmanager.app.model.Course
import com.lmstaskmanager.app.repository.TaskRepository
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
            assignments = DatabaseManager.getDatabase(context)
                .assignmentDao()
                .getAllAssignments()
                .sortedBy { it.dueDate }
        }
    }

    val courses = TaskRepository.courses
    val now = System.currentTimeMillis()
    val overdue = assignments.filter { !it.completed && it.dueDate < now }
    val upcoming = assignments.filter { !it.completed && it.dueDate >= now }
    val completed = assignments.filter { it.completed }

    fun toggle(assignment: AssignmentEntity) {
        val updated = assignment.copy(completed = !assignment.completed)
        assignments = assignments.map { if (it.id == updated.id) updated else it }
        CoroutineScope(Dispatchers.IO).launch {
            DatabaseManager.getDatabase(context).assignmentDao().upsertAssignment(updated)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Gray50)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Blue600)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Assignments",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = White
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (overdue.isNotEmpty()) {
                item {
                    SectionHeader(title = "Overdue", color = Red500)
                }
                items(overdue) { assignment ->
                    AssignmentCard(
                        assignment = assignment,
                        courses = courses,
                        isOverdue = true,
                        onToggle = { toggle(it) }
                    )
                }
            }

            if (upcoming.isNotEmpty()) {
                item {
                    SectionHeader(title = "Upcoming", color = Blue600)
                }
                items(upcoming) { assignment ->
                    AssignmentCard(
                        assignment = assignment,
                        courses = courses,
                        isOverdue = false,
                        onToggle = { toggle(it) }
                    )
                }
            }

            if (completed.isNotEmpty()) {
                item {
                    SectionHeader(title = "Completed", color = Gray400)
                }
                items(completed) { assignment ->
                    AssignmentCard(
                        assignment = assignment,
                        courses = courses,
                        isOverdue = false,
                        onToggle = { toggle(it) }
                    )
                }
            }

            if (assignments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Gray200, RoundedCornerShape(8.dp))
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No assignments", fontSize = 14.sp, color = Gray300)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = color)
        HorizontalDivider(modifier = Modifier.weight(1f), color = Gray200)
    }
}

@Composable
fun AssignmentCard(
    assignment: AssignmentEntity,
    courses: List<Course>,
    isOverdue: Boolean,
    onToggle: (AssignmentEntity) -> Unit
) {
    val course = courses.find { it.id == assignment.courseId }
    val courseColor = course?.color?.let { Color(it.toColorInt()) } ?: Gray300
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
                    .height(44.dp)
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
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(courseColor, CircleShape)
                    )
                    Text(text = course?.name ?: "", fontSize = 11.sp, color = Gray500)
                }
                Text(
                    text = "Due $dueText",
                    fontSize = 11.sp,
                    color = if (isOverdue) Red500 else Gray400
                )
            }
            IconButton(onClick = { onToggle(assignment) }) {
                Icon(
                    imageVector = if (assignment.completed)
                        Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = "Toggle",
                    tint = if (assignment.completed) Green500 else Gray300
                )
            }
        }
    }
}