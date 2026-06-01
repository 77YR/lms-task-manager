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

@Composable
fun CoursesScreen() {
    val courses = TaskRepository.courses
    val assignments = TaskRepository.assignments

    Column(modifier = Modifier.fillMaxSize().background(Gray50)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Blue600)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Courses",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = White
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(courses) { course ->
                val courseAssignments = assignments.filter { it.courseId == course.id }
                val courseColor = Color(course.color.toColorInt())

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = White),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(courseColor, CircleShape)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = course.name,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                                color = Gray900
                            )
                            Text(
                                text = course.teacher,
                                fontSize = 12.sp,
                                color = Gray500
                            )
                        }
                        Text(
                            text = "${courseAssignments.size} assignments",
                            fontSize = 11.sp,
                            color = Gray400
                        )
                    }
                }
            }
        }
    }
}