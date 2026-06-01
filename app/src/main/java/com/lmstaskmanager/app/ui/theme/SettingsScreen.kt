package com.lmstaskmanager.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lmstaskmanager.app.settings.AppSettings

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    var autoDeleteEnabled by remember {
        mutableStateOf(AppSettings.isAutoDeleteEnabled(context))
    }
    var assignmentDays by remember {
        mutableStateOf(AppSettings.getAssignmentDeleteDays(context).toFloat())
    }
    var taskDays by remember {
        mutableStateOf(AppSettings.getTaskDeleteDays(context).toFloat())
    }

    Column(modifier = Modifier.fillMaxSize().background(Gray50)) {
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
                Text(
                    text = "Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = White
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Auto-delete section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(0.dp),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-delete completed items",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = Gray900
                            )
                            Text(
                                text = "Remove old completed tasks and assignments",
                                fontSize = 12.sp,
                                color = Gray500
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Switch(
                            checked = autoDeleteEnabled,
                            onCheckedChange = {
                                autoDeleteEnabled = it
                                AppSettings.setAutoDeleteEnabled(context, it)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = White,
                                checkedTrackColor = Blue600
                            )
                        )
                    }

                    if (autoDeleteEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Gray200)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Assignment threshold
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Delete completed assignments after",
                                    fontSize = 13.sp,
                                    color = Gray900
                                )
                                Text(
                                    text = "${assignmentDays.toInt()} day(s)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Blue600
                                )
                            }
                            Slider(
                                value = assignmentDays,
                                onValueChange = {
                                    assignmentDays = it
                                    AppSettings.setAssignmentDeleteDays(context, it.toInt())
                                },
                                valueRange = 1f..7f,
                                steps = 5,
                                colors = SliderDefaults.colors(
                                    thumbColor = Blue600,
                                    activeTrackColor = Blue600
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Task threshold
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Delete done tasks after",
                                    fontSize = 13.sp,
                                    color = Gray900
                                )
                                Text(
                                    text = "${taskDays.toInt()} day(s)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Blue600
                                )
                            }
                            Slider(
                                value = taskDays,
                                onValueChange = {
                                    taskDays = it
                                    AppSettings.setTaskDeleteDays(context, it.toInt())
                                },
                                valueRange = 1f..7f,
                                steps = 5,
                                colors = SliderDefaults.colors(
                                    thumbColor = Blue600,
                                    activeTrackColor = Blue600
                                )
                            )
                        }
                    }
                }
            }

            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Blue100),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Text(
                    text = "Cleanup runs automatically each time the app starts.",
                    fontSize = 12.sp,
                    color = Blue600,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}