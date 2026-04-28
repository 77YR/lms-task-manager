package com.lmstaskmanager.app.ui.theme

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.lmstaskmanager.app.model.Task
import com.lmstaskmanager.app.model.TaskStatus
import com.lmstaskmanager.app.repository.TaskRepository

data class DragState(
    val isDragging: Boolean = false,
    val draggedTask: Task? = null,
    val dragPosition: Offset = Offset.Zero
)

@Composable
fun HomeScreen() {
    var tasks by remember { mutableStateOf(TaskRepository.tasks.toList()) }
    var dragState by remember { mutableStateOf(DragState()) }
    val columnBounds = remember { mutableMapOf<TaskStatus, androidx.compose.ui.geometry.Rect>() }

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
        elevation = CardDefaults.cardElevation(if (isDragging) 8.dp else 2.dp)
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