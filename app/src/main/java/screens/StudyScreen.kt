package com.student.planner.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.student.planner.data.StudyGoal
import com.student.planner.ui.theme.*
import com.student.planner.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(viewModel: MainViewModel) {
    val allGoals by viewModel.allStudyGoals.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var goalToEdit by remember { mutableStateOf<StudyGoal?>(null) }
    var goalToDelete by remember { mutableStateOf<StudyGoal?>(null) }

    val pendingGoals = allGoals.filter { !it.isDone }
    val doneGoals = allGoals.filter { it.isDone }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = PrimaryPurple
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal", tint = Color.White)
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PrimaryPurple)
                    .padding(16.dp)
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = "Study To-Do",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${pendingGoals.size} remaining · ${doneGoals.size} completed",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Goals list
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pending goals
                if (pendingGoals.isNotEmpty()) {
                    item {
                        Text(
                            text = "📚 To Study",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }
                    items(pendingGoals, key = { it.id }) { goal ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically()
                        ) {
                            StudyGoalCard(
                                goal = goal,
                                onToggleDone = {
                                    viewModel.updateStudyGoal(goal.copy(isDone = !goal.isDone))
                                },
                                onEdit = { goalToEdit = goal },
                                onDelete = { goalToDelete = goal }
                            )
                        }
                    }
                }

                // Done goals
                if (doneGoals.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✅ Completed",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }
                    items(doneGoals, key = { it.id }) { goal ->
                        StudyGoalCard(
                            goal = goal,
                            onToggleDone = {
                                viewModel.updateStudyGoal(goal.copy(isDone = !goal.isDone))
                            },
                            onEdit = { goalToEdit = goal },
                            onDelete = { goalToDelete = goal }
                        )
                    }
                }

                // Empty state
                if (allGoals.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "📖", fontSize = 56.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No study goals yet!",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Tap + to add what you need to study",
                                    fontSize = 14.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add/Edit Bottom Sheet
    if (showAddSheet || goalToEdit != null) {
        AddStudyGoalBottomSheet(
            goalToEdit = goalToEdit,
            onDismiss = {
                showAddSheet = false
                goalToEdit = null
            },
            onSave = { goal ->
                if (goalToEdit != null) {
                    viewModel.updateStudyGoal(goal)
                } else {
                    viewModel.insertStudyGoal(goal)
                }
                showAddSheet = false
                goalToEdit = null
            }
        )
    }

    // Delete confirmation
    goalToDelete?.let { goal ->
        AlertDialog(
            onDismissRequest = { goalToDelete = null },
            title = { Text("Delete Goal") },
            text = { Text("Delete \"${goal.courseName}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteStudyGoal(goal)
                    goalToDelete = null
                }) { Text("Delete", color = ExamRed) }
            },
            dismissButton = {
                TextButton(onClick = { goalToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudyGoalBottomSheet(
    goalToEdit: StudyGoal? = null,
    onDismiss: () -> Unit,
    onSave: (StudyGoal) -> Unit
) {
    var title by remember { mutableStateOf(goalToEdit?.courseName ?: "") }
    var note by remember { mutableStateOf(goalToEdit?.note ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (goalToEdit != null) "Edit Study Goal" else "Add Study Goal",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title (e.g. Math)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (e.g. Solve questions page 2)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (title.isNotEmpty()) {
                        onSave(
                            StudyGoal(
                                id = goalToEdit?.id ?: 0,
                                courseName = title,
                                note = note,
                                isDone = goalToEdit?.isDone ?: false
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
                    text = if (goalToEdit != null) "Save Changes" else "Add Goal",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun StudyGoalCard(
    goal: StudyGoal,
    onToggleDone: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    /*Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { showMenu = true })
            },*/
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (goal.isDone) SurfaceLight else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (goal.isDone) 0.dp else 2.dp
        )
    ) {
        /*Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),*/
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onLongPress = { showMenu = true })
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Checkbox button
            IconButton(
                onClick = onToggleDone,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = if (goal.isDone) PrimaryPurple else PrimaryPurple.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Toggle Done",
                    tint = if (goal.isDone) Color.White else PrimaryPurple,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Title and note
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.courseName,
                    fontSize = 16.sp,
                    fontWeight = if (goal.isDone) FontWeight.Normal else FontWeight.SemiBold,
                    color = if (goal.isDone) TextSecondary else TextPrimary,
                    textDecoration = if (goal.isDone) TextDecoration.LineThrough else TextDecoration.None
                )
                if (goal.note.isNotEmpty()) {
                    Text(
                        text = goal.note,
                        fontSize = 13.sp,
                        color = if (goal.isDone) TextSecondary.copy(alpha = 0.6f) else TextSecondary,
                        textDecoration = if (goal.isDone) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
            }
        }

        // Dropdown menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit") },
                leadingIcon = { Icon(Icons.Default.Edit, null) },
                onClick = {
                    showMenu = false
                    onEdit()
                }
            )
            DropdownMenuItem(
                text = { Text("Delete", color = ExamRed) },
                leadingIcon = {
                    Icon(Icons.Default.Delete, null, tint = ExamRed)
                },
                onClick = {
                    showMenu = false
                    onDelete()
                }
            )
        }
    }
}