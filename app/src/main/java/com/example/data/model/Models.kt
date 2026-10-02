package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class Priority(val label: String, val level: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3);

    companion object {
        fun fromString(value: String): Priority {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
        }
    }
}

enum class TaskCategory(val label: String) {
    WORK("Work"),
    PERSONAL("Personal"),
    HEALTH("Health"),
    LEARNING("Learning"),
    OTHER("Other");

    companion object {
        fun fromString(value: String): TaskCategory {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: WORK
        }
    }
}

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val notes: String = "",
    val priority: String = Priority.MEDIUM.name,
    val category: String = TaskCategory.WORK.name,
    val dueDateEpochDay: Long = LocalDate.now().toEpochDay(),
    val startTime: String = "", // e.g. "10:30"
    val endTime: String = "",   // e.g. "11:30"
    val isCompleted: Boolean = false,
    val completedAtTimestamp: Long? = null,
    val isRecurring: Boolean = false,
    val reminderEnabled: Boolean = false
)

@Entity(tableName = "habits")
data class HabitItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "Health",
    val targetDaysPerWeek: Int = 7,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val completedDatesCsv: String = "", // e.g. "2026-10-01,2026-10-02"
    val reminderTime: String = "08:00",
    val iconName: String = "fitness"
)

@Entity(tableName = "schedule_items")
data class ScheduleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val startTime: String, // e.g. "09:00"
    val endTime: String,   // e.g. "10:30"
    val dateEpochDay: Long = LocalDate.now().toEpochDay(),
    val category: String = "Work",
    val isCompleted: Boolean = false
)

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val durationMinutes: Int,
    val completedAtTimestamp: Long = System.currentTimeMillis(),
    val sessionType: String = "FOCUS", // "FOCUS" or "BREAK"
    val taskTitle: String = "Deep Work"
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Ajeet",
    val wakeUpTime: String = "06:30",
    val sleepTime: String = "23:00",
    val productivityGoal: String = "Master Daily Time & Focus",
    val isOnboardingCompleted: Boolean = false,
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val isHapticsEnabled: Boolean = true,
    val defaultFocusDuration: Int = 25,
    val defaultBreakDuration: Int = 5
)
