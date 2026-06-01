package com.lmstaskmanager.app.ui.theme

import java.util.UUID
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.graphics.toColorInt
import com.lmstaskmanager.app.database.DatabaseManager
import com.lmstaskmanager.app.database.TaskEntity
import com.lmstaskmanager.app.model.DataSource
import com.lmstaskmanager.app.model.Task
import com.lmstaskmanager.app.model.TaskStatus
import com.lmstaskmanager.app.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.material.icons.filled.Settings
import com.lmstaskmanager.app.navigation.Screen

data class DragState(
    val isDragging: Boolean = false,
    val draggedTask: Task? = null,
    val dragPosition: Offset = Offset.Zero
)

@Composable
fun HomeScreen(navController: androidx.navigation.NavController) {
    val context = LocalContext.current
    var tasks by remember { mutableStateOf(emptyList<Task>()) }
    var dragState by remember { mutableStateOf(DragState()) }
    val columnBounds = remember { mutableMapOf<TaskStatus, androidx.compose.ui.geometry.Rect>() }
    val density = LocalDensity.current
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }

    LaunchedEffect(Unit) {
        val db = DatabaseManager.getDatabase(context)
        tasks = db.taskDao().getAllTasks().map { entity ->
            Task(
                id = entity.id,
                title = entity.title,
                status = TaskStatus.valueOf(entity.status),
                courseId = entity.courseId,
                source = DataSource.valueOf(entity.source)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Gray50)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Blue600)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Tasks",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = White
                        )
                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = "Settings",
                                tint = White
                            )
                        }
                    }
                }
            }

            // Add button + Kanban
            item {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Blue600
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gray200)
                    ) {
                        Text("+ New Task", fontSize = 14.sp)
                    }

                    KanbanBoard(
                        tasks = tasks,
                        dragState = dragState,
                        onDragStart = { task, globalOffset ->
                            dragState = DragState(true, task, globalOffset)
                        },
                        onDrag = { dragAmount ->
                            dragState = dragState.copy(
                                dragPosition = dragState.dragPosition + dragAmount
                            )
                        },
                        onDragEnd = {
                            dragState.draggedTask?.let { task ->
                                val pos = dragState.dragPosition
                                val newStatus = columnBounds.entries.firstOrNull { (_, bounds) ->
                                    pos.x in bounds.left..bounds.right &&
                                            pos.y in bounds.top..bounds.bottom
                                }?.key
                                if (newStatus != null && newStatus != task.status) {
                                    tasks = tasks.map {
                                        if (it.id == task.id) it.copy(status = newStatus) else it
                                    }
                                    TaskRepository.updateTaskStatus(task.id, newStatus)
                                    CoroutineScope(Dispatchers.IO).launch {
                                        DatabaseManager.getDatabase(context)
                                            .taskDao()
                                            .updateTaskStatus(task.id, newStatus.name)
                                    }
                                }
                            }
                            dragState = DragState()
                        },
                        onTap = { task -> selectedTask = task },
                        onColumnBoundsChanged = { status, bounds ->
                            columnBounds[status] = bounds
                        }
                    )
                }
            }
        }

        // Edit dialog
        selectedTask?.let { task ->
            EditTaskDialog(
                task = task,
                onDismiss = { selectedTask = null },
                onSave = { updated ->
                    tasks = tasks.map { if (it.id == updated.id) updated else it }
                    TaskRepository.updateTaskStatus(updated.id, updated.status)
                    CoroutineScope(Dispatchers.IO).launch {
                        DatabaseManager.getDatabase(context).taskDao().upsertTask(
                            TaskEntity(
                                id = updated.id,
                                title = updated.title,
                                status = updated.status.name,
                                courseId = updated.courseId,
                                source = updated.source.name
                            )
                        )
                    }
                    selectedTask = null
                },
                onDelete = { taskToDelete ->
                    tasks = tasks.filter { it.id != taskToDelete.id }
                    CoroutineScope(Dispatchers.IO).launch {
                        DatabaseManager.getDatabase(context).taskDao().deleteTask(
                            TaskEntity(
                                id = taskToDelete.id,
                                title = taskToDelete.title,
                                status = taskToDelete.status.name,
                                courseId = taskToDelete.courseId,
                                source = taskToDelete.source.name
                            )
                        )
                    }
                    selectedTask = null
                }
            )
        }

        // Add dialog
        if (showAddDialog) {
            AddTaskDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { newTask ->
                    tasks = tasks + newTask
                    CoroutineScope(Dispatchers.IO).launch {
                        DatabaseManager.getDatabase(context).taskDao().upsertTask(
                            TaskEntity(
                                id = newTask.id,
                                title = newTask.title,
                                status = newTask.status.name,
                                courseId = newTask.courseId,
                                source = newTask.source.name
                            )
                        )
                    }
                    showAddDialog = false
                }
            )
        }

        // Drag shadow
        if (dragState.isDragging && dragState.draggedTask != null) {
            val task = dragState.draggedTask!!
            val course = TaskRepository.courses.find { it.id == task.courseId }
            val courseColor = course?.color?.let { Color(it.toColorInt()) } ?: Gray300

            Card(
                modifier = Modifier
                    .width(140.dp)
                    .zIndex(10f)
                    .offset {
                        IntOffset(
                            (dragState.dragPosition.x - with(density) { 70.dp.toPx() }).roundToInt(),
                            (dragState.dragPosition.y - with(density) { 30.dp.toPx() }).roundToInt()
                        )
                    },
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(text = task.title, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(6.dp))
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
                            fontSize = 10.sp,
                            color = Gray500
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KanbanBoard(
    tasks: List<Task>,
    dragState: DragState,
    onDragStart: (Task, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onTap: (Task) -> Unit,
    onColumnBoundsChanged: (TaskStatus, androidx.compose.ui.geometry.Rect) -> Unit
) {
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
                status = status,
                tasks = tasks.filter { it.status == status },
                dragState = dragState,
                onDragStart = onDragStart,
                onDrag = onDrag,
                onDragEnd = onDragEnd,
                onTap = onTap,
                onBoundsChanged = { bounds -> onColumnBoundsChanged(status, bounds) }
            )
        }
    }
}

@Composable
fun KanbanColumn(
    modifier: Modifier = Modifier,
    label: String,
    status: TaskStatus,
    tasks: List<Task>,
    dragState: DragState,
    onDragStart: (Task, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onTap: (Task) -> Unit,
    onBoundsChanged: (androidx.compose.ui.geometry.Rect) -> Unit
) {
    val isDropTarget = dragState.isDragging && dragState.draggedTask?.status != status

    Column(
        modifier = modifier
            .onGloballyPositioned { coords -> onBoundsChanged(coords.boundsInWindow()) }
            .background(
                if (isDropTarget) Blue100 else Gray100,
                RoundedCornerShape(10.dp)
            )
            .then(
                if (isDropTarget) Modifier.border(1.dp, Blue600, RoundedCornerShape(10.dp))
                else Modifier
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = Gray500)
            Text(text = "${tasks.size}", fontSize = 11.sp, color = Gray400)
        }

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .border(1.dp, Gray200, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Empty", fontSize = 11.sp, color = Gray300)
            }
        } else {
            tasks.forEach { task ->
                DraggableTaskCard(
                    task = task,
                    isDragging = dragState.draggedTask?.id == task.id,
                    onDragStart = onDragStart,
                    onDrag = onDrag,
                    onDragEnd = onDragEnd,
                    onTap = onTap
                )
            }
        }
    }
}

@Composable
fun DraggableTaskCard(
    task: Task,
    isDragging: Boolean,
    onDragStart: (Task, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onTap: (Task) -> Unit
) {
    val course = TaskRepository.courses.find { it.id == task.courseId }
    val courseColor = course?.color?.let { Color(it.toColorInt()) } ?: Gray300
    var cardWindowPosition by remember { mutableStateOf(Offset.Zero) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords -> cardWindowPosition = coords.positionInWindow() }
            .clickable { onTap(task) }
            .pointerInput(task) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { localOffset ->
                        onDragStart(task, cardWindowPosition + localOffset)
                    },
                    onDrag = { _, dragAmount -> onDrag(dragAmount) },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDragging) Gray100 else White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = task.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDragging) Color.Transparent else Gray900
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (isDragging) Color.Transparent else courseColor,
                            CircleShape
                        )
                )
                Text(
                    text = course?.name ?: "",
                    fontSize = 10.sp,
                    color = if (isDragging) Color.Transparent else Gray500,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (Task) -> Unit
) {
    val courses = TaskRepository.courses
    var title by remember { mutableStateOf("") }
    var selectedCourseId by remember { mutableStateOf(courses.first().id) }
    var selectedStatus by remember { mutableStateOf(TaskStatus.TODO) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Task", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = courses.find { it.id == selectedCourseId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Course") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        courses.forEach { course ->
                            DropdownMenuItem(
                                text = { Text(course.name, fontSize = 14.sp) },
                                onClick = { selectedCourseId = course.id; expanded = false }
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TaskStatus.entries.forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status },
                            label = { Text(status.name, fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank()) {
                    onConfirm(Task(
                        id = UUID.randomUUID().toString(),
                        title = title.trim(),
                        status = selectedStatus,
                        courseId = selectedCourseId,
                        source = DataSource.LOCAL
                    ))
                }
            }) { Text("Add", color = Blue600) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Gray500) }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit,
    onDelete: (Task) -> Unit
) {
    val courses = TaskRepository.courses
    var title by remember { mutableStateOf(task.title) }
    var selectedCourseId by remember { mutableStateOf(task.courseId) }
    var selectedStatus by remember { mutableStateOf(task.status) }
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Task") },
            text = { Text("Delete \"${task.title}\"?") },
            confirmButton = {
                TextButton(onClick = { onDelete(task) }) {
                    Text("Delete", color = Red500)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Edit Task", fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = courses.find { it.id == selectedCourseId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Course") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            courses.forEach { course ->
                                DropdownMenuItem(
                                    text = { Text(course.name, fontSize = 14.sp) },
                                    onClick = { selectedCourseId = course.id; expanded = false }
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TaskStatus.entries.forEach { status ->
                            FilterChip(
                                selected = selectedStatus == status,
                                onClick = { selectedStatus = status },
                                label = { Text(status.name, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showDeleteConfirm = true }) {
                        Text("Delete", color = Red500)
                    }
                    TextButton(onClick = {
                        if (title.isNotBlank()) {
                            onSave(task.copy(
                                title = title.trim(),
                                courseId = selectedCourseId,
                                status = selectedStatus
                            ))
                        }
                    }) { Text("Save", color = Blue600) }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Cancel", color = Gray500) }
            }
        )
    }
}