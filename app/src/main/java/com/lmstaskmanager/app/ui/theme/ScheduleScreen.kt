package com.lmstaskmanager.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
    val eventsByDay = (1..5).map { day -> day to events.filter { it.day == day } }

    Column(modifier = Modifier.fillMaxSize().background(Gray50)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Blue600)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Schedule",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = White
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(eventsByDay) { (day, dayEvents) ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = dayNames[day],
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Gray900
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Gray200
                        )
                    }

                    if (dayEvents.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Gray200, RoundedCornerShape(8.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No classes", fontSize = 12.sp, color = Gray300)
                        }
                    } else {
                        dayEvents.forEach { event ->
                            val course = courses.find { it.id == event.courseId }
                            val courseColor = course?.color?.let {
                                Color(it.toColorInt())
                            } ?: Gray300

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
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
                                            .background(courseColor, RoundedCornerShape(2.dp))
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = event.title,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp,
                                            color = Gray900
                                        )
                                        Text(
                                            text = "${event.startTime} – ${event.endTime}",
                                            fontSize = 12.sp,
                                            color = Gray500
                                        )
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(courseColor, CircleShape)
                                        )
                                        Text(
                                            text = course?.name ?: "",
                                            fontSize = 11.sp,
                                            color = Gray500
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
}