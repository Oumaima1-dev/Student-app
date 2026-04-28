package com.student.planner.screens







import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.student.planner.data.Course
import com.student.planner.data.Task
import com.student.planner.ui.theme.*
import com.student.planner.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(viewModel: MainViewModel) {
    val allCourses by viewModel.allCourses.collectAsState()
    val upcomingTasks by viewModel.upcomingTasks.collectAsState()

    val calendar = Calendar.getInstance()
    val todayDayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 1
        Calendar.TUESDAY -> 2
        Calendar.WEDNESDAY -> 3
        Calendar.THURSDAY -> 4
        Calendar.FRIDAY -> 5
        else -> 0
    }

    val todayCourses = allCourses.filter { it.dayOfWeek == todayDayOfWeek }
    val nearestTasks = upcomingTasks.take(3)

    val dayName = when (todayDayOfWeek) {
        1 -> "Monday"
        2 -> "Tuesday"
        3 -> "Wednesday"
        4 -> "Thursday"
        5 -> "Friday"
        else -> "Weekend"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Column {
                Text(
                    text = "Good ${getGreeting()} 👋",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Today, $dayName",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
        }

        item {
            // Today's Classes Section
            SectionHeader(
                title = "Today's Classes",
                icon = "📅",
                count = todayCourses.size
            )
        }

        if (todayCourses.isEmpty()) {
            item {
                EmptyCard(message = "No classes today! Enjoy your day 🎉")
            }
        } else {
            items(todayCourses) { course ->
                HomeCourseCard(course = course)
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            SectionHeader(
                title = "Upcoming Tasks",
                icon = "📋",
                count = nearestTasks.size
            )
        }

        if (nearestTasks.isEmpty()) {
            item {
                EmptyCard(message = "No upcoming tasks! You're all caught up ✅")
            }
        } else {
            items(nearestTasks) { task ->
                HomeTaskCard(task = task)
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: String, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = icon, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = PrimaryPurple.copy(alpha = 0.1f)
        ) {
            Text(
                text = "$count",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                color = PrimaryPurple,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun HomeCourseCard(course: Course) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    .height(48.dp)
                    .background(
                        color = PrimaryPurple,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))
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
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = course.startTime,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = PrimaryPurple
                )
                Text(
                    text = course.room,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun HomeTaskCard(task: Task) {
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

     /*val daysLeft = ((task.dueDate - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).toInt()*/
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
        modifier = Modifier.fillMaxWidth(),
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
                    .height(48.dp)
                    .background(
                        color = typeColor,
                        shape = RoundedCornerShape(2.dp)
                    )
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
            }
            Column(horizontalAlignment = Alignment.End) {
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = daysText,
                    fontSize = 12.sp,
                    color = if (daysLeft == 0) ExamRed else TextSecondary,
                    fontWeight = if (daysLeft == 0) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun EmptyCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = TextSecondary,
            fontSize = 14.sp
        )
    }
}

fun getGreeting(): String {
    return when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Morning"    // 5:00 AM - 11:59 AM
        in 12..17 -> "Afternoon" // 12:00 PM - 5:59 PM
        in 18..21 -> "Evening"   // 6:00 PM - 9:59 PM
        else -> "Night"          // 10:00 PM - 4:59 AM
    }
}


