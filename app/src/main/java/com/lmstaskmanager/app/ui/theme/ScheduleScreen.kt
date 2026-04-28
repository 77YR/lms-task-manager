package com.lmstaskmanager.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.lmstaskmanager.app.repository.TaskRepository

val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

@Composable
fun ScheduleScreen() {
    val events = TaskRepository.scheduleEvents
    val courses = TaskRepository.courses

    val eventsByDay = (1..5).map { day ->
        day to events.filter { it.day == day }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Weekly Schedule",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        items(eventsByDay) { (day, dayEvents) ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = dayNames[day],
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                if (dayEvents.isEmpty()) {
                    Text(
                        text = "No classes",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                } else {
                    dayEvents.forEach { event ->
                        val course = courses.find { it.id == event.courseId }
                        val courseColor = course?.color?.let {
                            Color(it.toColorInt())
                        } ?: Color.Gray

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
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
                                        .height(40.dp)
                                        .background(courseColor, RoundedCornerShape(2.dp))
                                )
                                Column {
                                    Text(
                                        text = event.title,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${event.startTime} - ${event.endTime}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}