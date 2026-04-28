package com.student.planner.screens
import java.util.Calendar
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.student.planner.data.Course
import com.student.planner.ui.theme.*
import com.student.planner.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(viewModel: MainViewModel) {
    val allCourses by viewModel.allCourses.collectAsState()
    var selectedDay by remember { mutableIntStateOf(getCurrentDayIndex()) }
    var showAddSheet by remember { mutableStateOf(false) }
    var courseToEdit by remember { mutableStateOf<Course?>(null) }
    var courseToDelete by remember { mutableStateOf<Course?>(null) }

    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")
    val filteredCourses = allCourses.filter { it.dayOfWeek == selectedDay + 1 }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = PrimaryPurple
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Course", tint = Color.White)
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
                    text = "My Schedule",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Tap a day to see your classes",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }

            // Day selector tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                days.forEachIndexed { index, day ->
                    val isSelected = selectedDay == index
                    Surface(
                        onClick = { selectedDay = index },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PrimaryPurple else SurfaceLight,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = day,
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = if (isSelected) Color.White else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Course list
            if (filteredCourses.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📭", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No classes on ${days[selectedDay]}",
                            color = TextSecondary,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredCourses, key = { it.id }) { course ->
                        ScheduleCourseCard(
                            course = course,
                            onEdit = { courseToEdit = course },
                            onDelete = { courseToDelete = course }
                        )
                    }
                }
            }
        }
    }

    // Add/Edit Bottom Sheet
    if (showAddSheet || courseToEdit != null) {
        AddCourseBottomSheet(
            courseToEdit = courseToEdit,
            onDismiss = {
                showAddSheet = false
                courseToEdit = null
            },
            onSave = { course ->
                if (courseToEdit != null) {
                    viewModel.updateCourse(course)
                } else {
                    viewModel.insertCourse(course)
                }
                showAddSheet = false
                courseToEdit = null
            }
        )
    }

    // Delete confirmation dialog
    courseToDelete?.let { course ->
        AlertDialog(
            onDismissRequest = { courseToDelete = null },
            title = { Text("Delete Course") },
            text = { Text("Are you sure you want to delete ${course.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCourse(course)
                    courseToDelete = null
                }) {
                    Text("Delete", color = ExamRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { courseToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ScheduleCourseCard(
    course: Course,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

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
            // Time column
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(56.dp)
            ) {
                Text(
                    text = course.startTime,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PrimaryPurple
                )
                Text(
                    text = course.endTime,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Divider
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(48.dp)
                    .background(
                        color = PrimaryPurple.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(1.dp)
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Course info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = course.professor,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                if (course.room.isNotEmpty()) {
                    Text(
                        text = "📍 ${course.room}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Dropdown menu for edit/delete
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                onClick = {
                    showMenu = false
                    onEdit()
                }
            )
            DropdownMenuItem(
                text = { Text("Delete", color = ExamRed) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = ExamRed
                    )
                },
                onClick = {
                    showMenu = false
                    onDelete()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCourseBottomSheet(
    courseToEdit: Course? = null,
    onDismiss: () -> Unit,
    onSave: (Course) -> Unit
) {
    var name by remember { mutableStateOf(courseToEdit?.name ?: "") }
    var professor by remember { mutableStateOf(courseToEdit?.professor ?: "") }
    var room by remember { mutableStateOf(courseToEdit?.room ?: "") }
    var startTime by remember { mutableStateOf(courseToEdit?.startTime ?: "") }
    var endTime by remember { mutableStateOf(courseToEdit?.endTime ?: "") }
    var selectedDay by remember { mutableIntStateOf((courseToEdit?.dayOfWeek ?: 1) - 1) }

    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (courseToEdit != null) "Edit Course" else "Add Course",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Course Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = professor,
                onValueChange = { professor = it },
                label = { Text("Professor") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = room,
                onValueChange = { room = it },
                label = { Text("Room (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("Start (09:00)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("End (10:30)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Day selector
            Text("Day", fontWeight = FontWeight.Medium, color = TextPrimary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                days.forEachIndexed { index, day ->
                    FilterChip(
                        selected = selectedDay == index,
                        onClick = { selectedDay = index },
                        label = { Text(day) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryPurple,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (name.isNotEmpty()) {
                        onSave(
                            Course(
                                id = courseToEdit?.id ?: 0,
                                name = name,
                                professor = professor,
                                room = room,
                                dayOfWeek = selectedDay + 1,
                                startTime = startTime,
                                endTime = endTime
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
                    text = if (courseToEdit != null) "Save Changes" else "Add Course",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun getCurrentDayIndex(): Int {
    return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 0
        Calendar.TUESDAY -> 1
        Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3
        Calendar.FRIDAY -> 4
        else -> 0
    }
}
