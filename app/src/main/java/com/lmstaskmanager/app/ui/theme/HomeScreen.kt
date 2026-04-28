package com.lmstaskmanager.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.lmstaskmanager.app.model.Task
import com.lmstaskmanager.app.model.TaskStatus
import com.lmstaskmanager.app.repository.TaskRepository

@Composable
fun HomeScreen() {
    val tasks = remember { TaskRepository.tasks }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "My Tasks",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        item {
            KanbanBoard(tasks = tasks)
        }
    }
}

@Composable
fun KanbanBoard(tasks: List<Task>) {
    val columns = listOf(
        TaskStatus.TODO to "To Do",
        TaskStatus.DOING to "Doing",
        TaskStatus.DONE to "Done"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        columns.forEach { (status, label) ->
            KanbanColumn(
                modifier = Modifier.weight(1f),
                label = label,
                tasks = tasks.filter { it.status == status }
            )
        }
    }
}

@Composable
fun KanbanColumn(
    modifier: Modifier = Modifier,
    label: String,
    tasks: List<Task>
) {
    Column(
        modifier = modifier
            .background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
        tasks.forEach { task ->
            TaskCard(task = task)
        }
    }
}

@Composable
fun TaskCard(task: Task) {
    val course = TaskRepository.courses.find { it.id == task.courseId }
    val courseColor = course?.color?.let {
        Color(it.toColorInt())
    } ?: Color.Gray

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = task.title, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .background(courseColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = course?.name ?: "No Course",
                    fontSize = 9.sp,
                    color = Color.White
                )
            }
        }
    }
}