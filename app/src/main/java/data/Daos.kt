package com.student.planner.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllCourses(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE dayOfWeek = :day ORDER BY startTime ASC")
    fun getCoursesByDay(day: Int): Flow<List<Course>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Update
    suspend fun updateCourse(course: Course)

    @Delete
    suspend fun deleteCourse(course: Course)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE dueDate >= :today ORDER BY dueDate ASC")
    fun getUpcomingTasks(today: Long): Flow<List<Task>>

    @Query("DELETE FROM tasks WHERE dueDate < :today")
    suspend fun deleteExpiredTasks(today: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
}

@Dao
interface StudyGoalDao {
    @Query("SELECT * FROM study_goals ORDER BY isDone ASC, createdAt DESC")
    fun getAllStudyGoals(): Flow<List<StudyGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyGoal(goal: StudyGoal)

    @Update
    suspend fun updateStudyGoal(goal: StudyGoal)

    @Delete
    suspend fun deleteStudyGoal(goal: StudyGoal)
}