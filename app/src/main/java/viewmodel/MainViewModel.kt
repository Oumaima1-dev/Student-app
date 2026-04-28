package com.student.planner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.student.planner.data.*
import com.student.planner.repository.Repository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = Repository(db)

    // ─── Courses ───────────────────────────────────────
    val allCourses = repository.getAllCourses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getCoursesByDay(day: Int) = repository.getCoursesByDay(day)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertCourse(course: Course) = viewModelScope.launch {
        repository.insertCourse(course)
    }

    fun updateCourse(course: Course) = viewModelScope.launch {
        repository.updateCourse(course)
    }

    fun deleteCourse(course: Course) = viewModelScope.launch {
        repository.deleteCourse(course)
    }

    // ─── Tasks ─────────────────────────────────────────
    val upcomingTasks = repository.getUpcomingTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertTask(task: Task) = viewModelScope.launch {
        repository.insertTask(task)
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        repository.updateTask(task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        repository.deleteTask(task)
    }

    fun deleteExpiredTasks() = viewModelScope.launch {
        repository.deleteExpiredTasks()
    }

    // ─── Study Goals ───────────────────────────────────
    val allStudyGoals = repository.getAllStudyGoals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertStudyGoal(goal: StudyGoal) = viewModelScope.launch {
        repository.insertStudyGoal(goal)
    }

    fun updateStudyGoal(goal: StudyGoal) = viewModelScope.launch {
        repository.updateStudyGoal(goal)
    }

    fun deleteStudyGoal(goal: StudyGoal) = viewModelScope.launch {
        repository.deleteStudyGoal(goal)
    }

    // ─── Auto cleanup on start ─────────────────────────
    init {
        deleteExpiredTasks()
    }
}