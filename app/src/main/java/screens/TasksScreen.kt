package com.student.planner.screens






import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.student.planner.data.Task
import com.student.planner.ui.theme.*
import com.student.planner.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(viewModel: MainViewModel) {
    val allTasks by viewModel.upcomingTasks.collectAsState()
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showAddSheet by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(1) }
    val endMonth = remember { currentMonth.plusMonths(3) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    // Map tasks to their dates
    val taskDateMap = allTasks.groupBy { task ->
        task.dueDate.let {
            java.time.Instant.ofEpochMilli(it)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
    }

    val selectedDayTasks = taskDateMap[selectedDate] ?: emptyList()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = PrimaryPurple
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundWhite)
                .padding(padding)
        ) {
            // Header
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "My Tasks",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Tap a date to see tasks",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }

            // Calendar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    // Month header
                    val visibleMonth = calendarState.firstVisibleMonth
                    Text(
                        text = visibleMonth.yearMonth.format(
                            DateTimeFormatter.ofPattern("MMMM yyyy")
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )

                    // Day of week headers
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }
                    }

                    HorizontalCalendar(
                        state = calendarState,
                        dayContent = { day ->
                            CalendarDayView(
                                day = day,
                                isSelected = day.date == selectedDate,
                                hasTask = taskDateMap.containsKey(day.date),
                                taskTypes = taskDateMap[day.date]?.map { it.type } ?: emptyList(),
                                onClick = {
                                    if (day.position == DayPosition.MonthDate) {
                                        selectedDate = day.date
                                    }
                                }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tasks for selected date
            val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")
            Text(
                text = if (selectedDate == LocalDate.now()) "Today's Tasks"
                else selectedDate.format(formatter),
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedDayTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks on this date ✨",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(selectedDayTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onEdit = { taskToEdit = task },
                            onDelete = { taskToDelete = task }
                        )
                    }
                }
            }
        }
    }

    // Add/Edit Bottom Sheet
    if (showAddSheet || taskToEdit != null) {
        AddTaskBottomSheet(
            taskToEdit = taskToEdit,
            onDismiss = {
                showAddSheet = false
                taskToEdit = null
            },
            onSave = { task ->
                if (taskToEdit != null) {
                    viewModel.updateTask(task)
                } else {
                    viewModel.insertTask(task)
                }
                showAddSheet = false
                taskToEdit = null
            }
        )
    }

    // Delete confirmation
    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Delete Task") },
            text = { Text("Delete \"${task.title}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTask(task)
                    taskToDelete = null
                }) { Text("Delete", color = ExamRed) }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun CalendarDayView(
    day: CalendarDay,
    isSelected: Boolean,
    hasTask: Boolean,
    taskTypes: List<String>,
    onClick: () -> Unit
) {
    val isToday = day.date == LocalDate.now()
    val isCurrentMonth = day.position == DayPosition.MonthDate

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(
                color = when {
                    isSelected -> PrimaryPurple
                    isToday -> PrimaryPurple.copy(alpha = 0.15f)
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) },
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.date.dayOfMonth.toString(),
                fontSize = 13.sp,
                color = when {
                    isSelected -> Color.White
                    !isCurrentMonth -> TextSecondary.copy(alpha = 0.3f)
                    isToday -> PrimaryPurple
                    else -> TextPrimary
                },
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
            )
            if (hasTask && isCurrentMonth) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    taskTypes.distinct().take(3).forEach { type ->
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(
                                    color = if (isSelected) Color.White else when (type) {
                                        "EXAM" -> ExamRed
                                        "QUIZ" -> QuizYellow
                                        else -> AssignmentBlue
                                    },
                                    shape = RoundedCornerShape(50)
                                )
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val typeColor = when (task.type) {
        "EXAM" -> ExamRed
        "QUIZ" -> QuizYellow
        else -> AssignmentBlue
    }
    val typeLabel = when (task.type) {
        "EXAM" -> "Exam"
        "QUIZ" -> "Quiz"
        else -> "Assignment"
    }
    /*  val daysLeft = ((task.dueDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt() */



    val today = java.time.LocalDate.now()
    val due = java.time.Instant.ofEpochMilli(task.dueDate)
        .atZone(java.time.ZoneId.systemDefault())
        .toLocalDate()

    val daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, due).toInt()























    val daysText = when {
        daysLeft == 0 -> "Due today!"
        daysLeft == 1 -> "Tomorrow"
        else -> "In $daysLeft days"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { showMenu = true })
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(52.dp)
                    .background(typeColor, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = task.courseName,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Text(
                    text = daysText,
                    fontSize = 12.sp,
                    color = if (daysLeft == 0) ExamRed else TextSecondary,
                    fontWeight = if (daysLeft == 0) FontWeight.Bold else FontWeight.Normal
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = typeColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = typeLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = typeColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit") },
                leadingIcon = { Icon(Icons.Default.Edit, null) },
                onClick = { showMenu = false; onEdit() }
            )
            DropdownMenuItem(
                text = { Text("Delete", color = ExamRed) },
                leadingIcon = { Icon(Icons.Default.Delete, null, tint = ExamRed) },
                onClick = { showMenu = false; onDelete() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskBottomSheet(
    taskToEdit: Task? = null,
    onDismiss: () -> Unit,
    onSave: (Task) -> Unit
) {
   /* var title by remember { mutableStateOf(taskToEdit?.title ?: "") } */
    var courseName by remember { mutableStateOf(taskToEdit?.courseName ?: "") }
    var selectedType by remember { mutableStateOf(taskToEdit?.type ?: "ASSIGNMENT") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val types = listOf("ASSIGNMENT", "QUIZ", "EXAM")
    val typeColors = mapOf(
        "ASSIGNMENT" to AssignmentBlue,
        "QUIZ" to QuizYellow,
        "EXAM" to ExamRed
    )
    val typeLabels = mapOf(
        "ASSIGNMENT" to "Assignment",
        "QUIZ" to "Quiz",
        "EXAM" to "Exam"
    )

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (taskToEdit != null) "Edit Task" else "Add Task",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

           /* OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) */

            OutlinedTextField(
                value = courseName,
                onValueChange = { courseName = it },
                label = { Text("Course Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Type selector
            Text("Type", fontWeight = FontWeight.Medium, color = TextPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                types.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = { Text(typeLabels[type] ?: type) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = typeColors[type] ?: PrimaryPurple,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Date picker
            /* Text("Due Date", fontWeight = FontWeight.Medium, color = TextPrimary)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Simple date buttons for next 14 days
                LazyColumn(modifier = Modifier.height(120.dp)) {
                    items((0..30).toList()) { offset ->
                        val date = LocalDate.now().plusDays(offset.toLong())
                        val isSelected = date == selectedDate
                        Surface(
                            onClick = { selectedDate = date },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) PrimaryPurple else SurfaceLight,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = if (offset == 0) "Today"
                                else if (offset == 1) "Tomorrow"
                                else date.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                color = if (isSelected) Color.White else TextPrimary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } */
            // Date picker
            Text("Due Date", fontWeight = FontWeight.Medium, color = TextPrimary)

            var showDatePicker by remember { mutableStateOf(false) }

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy")),
                    color = TextPrimary
                )
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = selectedDate
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                selectedDate = java.time.Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                            }
                            showDatePicker = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                   /* if (title.isNotEmpty() && courseName.isNotEmpty()) { */
                    if (courseName.isNotEmpty()) {
                        val dueDateMillis = selectedDate
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                        onSave(

                            /* Task(
                                id = taskToEdit?.id ?: 0,
                                title = title,
                                courseName = courseName,
                                type = selectedType,
                                dueDate = dueDateMillis
                            ) */








                           /* Task(
                                id = taskToEdit?.id ?: 0,
                                title = title,
                                courseName = courseName,
                                type = selectedType,
                                dueDate = dueDateMillis
                            ) */
                            Task(
                                id = taskToEdit?.id ?: 0,
                                title = courseName,
                                courseName = courseName,
                                type = selectedType,
                                dueDate = dueDateMillis
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
            ) {
                Text(
                    text = if (taskToEdit != null) "Save Changes" else "Add Task",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}



