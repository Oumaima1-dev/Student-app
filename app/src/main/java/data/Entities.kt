package com.student.planner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val professor: String,
    val room: String,
    val dayOfWeek: Int, // 1=Monday, 2=Tuesday, etc
    val startTime: String, // "09:00"
    val endTime: String,   // "10:30"
    val color: Long = 0xFF6200EE
)

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val courseName: String,
    val type: String, // "EXAM", "QUIZ", "ASSIGNMENT"
    val dueDate: Long, // stored as timestamp
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_goals")
data class StudyGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val courseName: String,
    val note: String = "",
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)