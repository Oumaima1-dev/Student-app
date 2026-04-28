package com.student.planner.repository

import com.student.planner.data.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class Repository(private val db: AppDatabase) {

    // ─── Courses ───────────────────────────────────────
    fun getAllCourses(): Flow<List<Course>> = db.courseDao().getAllCourses()
    fun getCoursesByDay(day: Int): Flow<List<Course>> = db.courseDao().getCoursesByDay(day)
    suspend fun insertCourse(course: Course) = db.courseDao().insertCourse(course)
    suspend fun updateCourse(course: Course) = db.courseDao().updateCourse(course)
    suspend fun deleteCourse(course: Course) = db.courseDao().deleteCourse(course)

    // ─── Tasks ─────────────────────────────────────────
    fun getAllTasks(): Flow<List<Task>> = db.taskDao().getAllTasks()
    fun getUpcomingTasks(): Flow<List<Task>> {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        return db.taskDao().getUpcomingTasks(today)
    }
    suspend fun insertTask(task: Task) = db.taskDao().insertTask(task)
    suspend fun updateTask(task: Task) = db.taskDao().updateTask(task)
    suspend fun deleteTask(task: Task) = db.taskDao().deleteTask(task)
    suspend fun deleteExpiredTasks() {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        db.taskDao().deleteExpiredTasks(today)
    }

    // ─── Study Goals ───────────────────────────────────
    fun getAllStudyGoals(): Flow<List<StudyGoal>> = db.studyGoalDao().getAllStudyGoals()
    suspend fun insertStudyGoal(goal: StudyGoal) = db.studyGoalDao().insertStudyGoal(goal)
    suspend fun updateStudyGoal(goal: StudyGoal) = db.studyGoalDao().updateStudyGoal(goal)
    suspend fun deleteStudyGoal(goal: StudyGoal) = db.studyGoalDao().deleteStudyGoal(goal)
}