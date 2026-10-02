package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FocusSession
import com.example.data.model.HabitItem
import com.example.data.model.ScheduleItem
import com.example.data.model.TaskItem
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeFlowDao {

    // Tasks
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, dueDateEpochDay ASC, startTime ASC")
    fun getAllTasks(): Flow<List<TaskItem>>

    @Query("SELECT * FROM tasks WHERE dueDateEpochDay = :dateEpochDay ORDER BY isCompleted ASC, startTime ASC")
    fun getTasksForDate(dateEpochDay: Long): Flow<List<TaskItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskItem): Long

    @Update
    suspend fun updateTask(task: TaskItem)

    @Delete
    suspend fun deleteTask(task: TaskItem)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    // Habits
    @Query("SELECT * FROM habits ORDER BY id ASC")
    fun getAllHabits(): Flow<List<HabitItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitItem): Long

    @Update
    suspend fun updateHabit(habit: HabitItem)

    @Delete
    suspend fun deleteHabit(habit: HabitItem)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: Long)

    // Schedule Items (Timeline)
    @Query("SELECT * FROM schedule_items WHERE dateEpochDay = :dateEpochDay ORDER BY startTime ASC")
    fun getScheduleForDate(dateEpochDay: Long): Flow<List<ScheduleItem>>

    @Query("SELECT * FROM schedule_items ORDER BY dateEpochDay ASC, startTime ASC")
    fun getAllScheduleItems(): Flow<List<ScheduleItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleItem(item: ScheduleItem): Long

    @Update
    suspend fun updateScheduleItem(item: ScheduleItem)

    @Delete
    suspend fun deleteScheduleItem(item: ScheduleItem)

    // Focus Sessions
    @Query("SELECT * FROM focus_sessions ORDER BY completedAtTimestamp DESC")
    fun getAllFocusSessions(): Flow<List<FocusSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(session: FocusSession): Long

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    @Query("DELETE FROM tasks")
    suspend fun clearTasks()

    @Query("DELETE FROM habits")
    suspend fun clearHabits()

    @Query("DELETE FROM schedule_items")
    suspend fun clearSchedule()

    @Query("DELETE FROM focus_sessions")
    suspend fun clearFocusSessions()
}
