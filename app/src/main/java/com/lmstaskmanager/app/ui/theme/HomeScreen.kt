package com.lmstaskmanager.app.ui.theme

import java.util.UUID
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lmstaskmanager.app.database.TaskEntity
import androidx.compose.ui.Alignment
import androidx.compose.ui.zIndex
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.core.graphics.toColorInt
import com.lmstaskmanager.app.model.Task
import com.lmstaskmanager.app.model.TaskStatus
import com.lmstaskmanager.app.repository.TaskRepository
import kotlin.math.roundToInt
import androidx.compose.runtime.LaunchedEffect
import com.lmstaskmanager.app.database.DatabaseManager
import com.lmstaskmanager.app.model.DataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class DragState(
    val isDragging: Boolean = false,
    val draggedTask: Task? = null,
    val dragPosition: Offset = Offset.Zero
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var tasks by remember { mutableStateOf(TaskRepository.tasks.toList()) }
    var dragState by remember { mutableStateOf(DragState()) }
    val columnBounds = remember { mutableMapOf<TaskStatus, androidx.compose.ui.geometry.Rect>() }
    val density = LocalDensity.current
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val db = DatabaseManager.getDatabase(context)
        val entities = db.taskDao().getAllTasks()
        tasks = entities.map { entity ->
            Task(
                id = entity.id,
                title = entity.title,
                status = TaskStatus.valueOf(entity.status),
                courseId = entity.courseId,
                source = DataSource.valueOf(entity.source)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "My Tasks", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Button(onClick = { showAddDialog = true }) {
                        Text("+ Add Task")
                    }
                }
            }
            item {
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
                    onColumnBoundsChanged = { status, bounds ->
                        columnBounds[status] = bounds
                    }
                )
            }
        }

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

        // Drag shadow overlay
        if (dragState.isDragging && dragState.draggedTask != null) {
            val task = dragState.draggedTask!!
            val course = TaskRepository.courses.find { it.id == task.courseId }
            val courseColor = course?.color?.let { Color(it.toColorInt()) } ?: Color.Gray

            Card(
                modifier = Modifier
                    .width(120.dp)
                    .zIndex(10f)
                    .offset {
                        IntOffset(
                            (dragState.dragPosition.x - with(density) { 60.dp.toPx() }).roundToInt(),
                            (dragState.dragPosition.y - with(density) { 30.dp.toPx() }).roundToInt()
                        )
                    },
                shape = RoundedCornerShape(6.dp),
                elevation = CardDefaults.cardElevation(12.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = task.title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
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
    }
}

@Composable
fun KanbanBoard(
    tasks: List<Task>,
    dragState: DragState,
    onDragStart: (Task, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
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
    onBoundsChanged: (androidx.compose.ui.geometry.Rect) -> Unit
) {
    val isDropTarget = dragState.isDragging &&
            dragState.draggedTask?.status != status

    Column(
        modifier = modifier
            .onGloballyPositioned { coords ->
                onBoundsChanged(coords.boundsInWindow())
            }
            .background(
                if (isDropTarget) Color(0xFFBBDEFB) else Color(0xFFE0E0E0),
                RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp
        )
        tasks.forEach { task ->
            DraggableTaskCard(
                task = task,
                isDragging = dragState.draggedTask?.id == task.id,
                onDragStart = onDragStart,
                onDrag = onDrag,
                onDragEnd = onDragEnd
            )
        }
    }
}

@Composable
fun DraggableTaskCard(
    task: Task,
    isDragging: Boolean,
    onDragStart: (Task, Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
) {
    val course = TaskRepository.courses.find { it.id == task.courseId }
    val courseColor = course?.color?.let {
        Color(it.toColorInt())
    } ?: Color.Gray

    var cardWindowPosition by remember { mutableStateOf(Offset.Zero) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isDragging) Modifier.background(Color.Transparent)
                else Modifier
            )
            .onGloballyPositioned { coords ->
                cardWindowPosition = coords.positionInWindow()
            }
            .pointerInput(task) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { localOffset ->
                        val globalOffset = cardWindowPosition + localOffset
                        onDragStart(task, globalOffset)
                    },
                    onDrag = { _, dragAmount ->
                        onDrag(dragAmount)
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() }
                )
            },
        shape = RoundedCornerShape(6.dp),
        elevation = CardDefaults.cardElevation(if (isDragging) 0.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .then(
                    if (isDragging) Modifier.background(Color(0xFFEEEEEE))
                    else Modifier
                )
        ) {
            Text(
                text = task.title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isDragging) Color.Transparent else Color.Unspecified
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .background(
                        if (isDragging) Color.Transparent else courseColor,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = course?.name ?: "No Course",
                    fontSize = 9.sp,
                    color = Color.Transparent
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
        title = { Text("New Task", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Course dropdown
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        courses.forEach { course ->
                            DropdownMenuItem(
                                text = { Text(course.name) },
                                onClick = {
                                    selectedCourseId = course.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Status selector
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskStatus.entries.forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = { selectedStatus = status },
                            label = {
                                Text(
                                    text = status.name,
                                    fontSize = 11.sp
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onConfirm(
                            Task(
                                id = UUID.randomUUID().toString(),
                                title = title.trim(),
                                status = selectedStatus,
                                courseId = selectedCourseId,
                                source = DataSource.LOCAL
                            )
                        )
                    }
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}