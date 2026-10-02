package com.example.data.repository

import com.example.data.local.TimeFlowDao
import com.example.data.model.FocusSession
import com.example.data.model.HabitItem
import com.example.data.model.Priority
import com.example.data.model.ScheduleItem
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItem
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TimeFlowRepository(private val dao: TimeFlowDao) {

    val allTasks: Flow<List<TaskItem>> = dao.getAllTasks()
    val allHabits: Flow<List<HabitItem>> = dao.getAllHabits()
    val allScheduleItems: Flow<List<ScheduleItem>> = dao.getAllScheduleItems()
    val allFocusSessions: Flow<List<FocusSession>> = dao.getAllFocusSessions()
    val userProfile: Flow<UserProfile?> = dao.getUserProfile()

    fun getTasksForDate(dateEpochDay: Long): Flow<List<TaskItem>> = dao.getTasksForDate(dateEpochDay)
    fun getScheduleForDate(dateEpochDay: Long): Flow<List<ScheduleItem>> = dao.getScheduleForDate(dateEpochDay)

    suspend fun insertTask(task: TaskItem): Long = dao.insertTask(task)
    suspend fun updateTask(task: TaskItem) = dao.updateTask(task)
    suspend fun deleteTask(task: TaskItem) = dao.deleteTask(task)
    suspend fun deleteTaskById(id: Long) = dao.deleteTaskById(id)

    suspend fun insertHabit(habit: HabitItem): Long = dao.insertHabit(habit)
    suspend fun updateHabit(habit: HabitItem) = dao.updateHabit(habit)
    suspend fun deleteHabit(habit: HabitItem) = dao.deleteHabit(habit)
    suspend fun deleteHabitById(id: Long) = dao.deleteHabitById(id)

    suspend fun insertScheduleItem(item: ScheduleItem): Long = dao.insertScheduleItem(item)
    suspend fun updateScheduleItem(item: ScheduleItem) = dao.updateScheduleItem(item)
    suspend fun deleteScheduleItem(item: ScheduleItem) = dao.deleteScheduleItem(item)

    suspend fun insertFocusSession(session: FocusSession): Long = dao.insertFocusSession(session)

    suspend fun saveUserProfile(profile: UserProfile) = dao.saveUserProfile(profile)

    suspend fun seedSampleDataIfNeeded(force: Boolean = false) {
        val today = LocalDate.now()
        val todayEpoch = today.toEpochDay()
        val yesterday = today.minusDays(1)
        val dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        if (force) {
            dao.clearTasks()
            dao.clearHabits()
            dao.clearSchedule()
            dao.clearFocusSessions()
        }

        // Profile default
        val defaultProfile = UserProfile(
            id = 1,
            name = "Ajeet",
            wakeUpTime = "06:30",
            sleepTime = "23:00",
            productivityGoal = "Master Daily Time & Focus",
            isOnboardingCompleted = true,
            themeMode = "SYSTEM",
            isHapticsEnabled = true,
            defaultFocusDuration = 25,
            defaultBreakDuration = 5
        )
        dao.saveUserProfile(defaultProfile)

        // Seed 7 sample tasks for today (5 completed, 2 remaining to match prompt: "5 of 7 completed, 72%")
        val sampleTasks = listOf(
            TaskItem(
                title = "Morning Mindset & Meditation",
                notes = "10 minutes guided mindfulness breathing",
                priority = Priority.MEDIUM.name,
                category = TaskCategory.HEALTH.name,
                dueDateEpochDay = todayEpoch,
                startTime = "07:00",
                endTime = "07:15",
                isCompleted = true,
                completedAtTimestamp = System.currentTimeMillis() - 1000 * 60 * 180
            ),
            TaskItem(
                title = "Review Weekly TimeFlow Plan",
                notes = "Check top 3 priorities for the sprint",
                priority = Priority.HIGH.name,
                category = TaskCategory.WORK.name,
                dueDateEpochDay = todayEpoch,
                startTime = "08:15",
                endTime = "08:45",
                isCompleted = true,
                completedAtTimestamp = System.currentTimeMillis() - 1000 * 60 * 150
            ),
            TaskItem(
                title = "Design System Review & Polish",
                notes = "Inspect component paddings and dark mode contrast",
                priority = Priority.HIGH.name,
                category = TaskCategory.WORK.name,
                dueDateEpochDay = todayEpoch,
                startTime = "09:00",
                endTime = "10:15",
                isCompleted = true,
                completedAtTimestamp = System.currentTimeMillis() - 1000 * 60 * 90
            ),
            TaskItem(
                title = "Hydration & Desk Stretch",
                notes = "Refill 1L water bottle and shoulder rolls",
                priority = Priority.LOW.name,
                category = TaskCategory.HEALTH.name,
                dueDateEpochDay = todayEpoch,
                startTime = "10:15",
                endTime = "10:30",
                isCompleted = true,
                completedAtTimestamp = System.currentTimeMillis() - 1000 * 60 * 30
            ),
            TaskItem(
                title = "Team Standup Sync",
                notes = "Update on sprint deliverables and blocker removals",
                priority = Priority.MEDIUM.name,
                category = TaskCategory.WORK.name,
                dueDateEpochDay = todayEpoch,
                startTime = "10:30",
                endTime = "11:00",
                isCompleted = true,
                completedAtTimestamp = System.currentTimeMillis() - 1000 * 60 * 10
            ),
            TaskItem(
                title = "Client Presentation & Roadmap",
                notes = "Present Q4 timeline architecture and milestones",
                priority = Priority.HIGH.name,
                category = TaskCategory.WORK.name,
                dueDateEpochDay = todayEpoch,
                startTime = "14:00",
                endTime = "15:00",
                isCompleted = false,
                reminderEnabled = true
            ),
            TaskItem(
                title = "Read 20 Pages: Atomic Habits",
                notes = "Chapter on cue design and friction reduction",
                priority = Priority.MEDIUM.name,
                category = TaskCategory.LEARNING.name,
                dueDateEpochDay = todayEpoch,
                startTime = "21:30",
                endTime = "22:15",
                isCompleted = false,
                isRecurring = true
            )
        )

        for (task in sampleTasks) {
            dao.insertTask(task)
        }

        // Daily Planner Timeline schedule items (as shown in user specification)
        val scheduleList = listOf(
            ScheduleItem(title = "Wake Up & Hydrate", startTime = "06:30", endTime = "07:00", dateEpochDay = todayEpoch, category = "Routine", isCompleted = true),
            ScheduleItem(title = "Morning Workout", startTime = "07:00", endTime = "08:00", dateEpochDay = todayEpoch, category = "Health", isCompleted = true),
            ScheduleItem(title = "Healthy Breakfast", startTime = "08:00", endTime = "09:00", dateEpochDay = todayEpoch, category = "Health", isCompleted = true),
            ScheduleItem(title = "Deep Focus: Project Architecture", startTime = "09:00", endTime = "11:00", dateEpochDay = todayEpoch, category = "Work", isCompleted = true),
            ScheduleItem(title = "Client Alignment Meeting", startTime = "11:00", endTime = "12:00", dateEpochDay = todayEpoch, category = "Work", isCompleted = true),
            ScheduleItem(title = "Nutritious Lunch & Walk", startTime = "13:00", endTime = "14:00", dateEpochDay = todayEpoch, category = "Personal", isCompleted = false),
            ScheduleItem(title = "Deep Work Session", startTime = "14:00", endTime = "17:00", dateEpochDay = todayEpoch, category = "Work", isCompleted = false),
            ScheduleItem(title = "Family Time & Dinner", startTime = "18:00", endTime = "20:00", dateEpochDay = todayEpoch, category = "Personal", isCompleted = false),
            ScheduleItem(title = "Reading & Reflection", startTime = "21:30", endTime = "22:30", dateEpochDay = todayEpoch, category = "Learning", isCompleted = false),
            ScheduleItem(title = "Sleep & Restoration", startTime = "23:00", endTime = "06:30", dateEpochDay = todayEpoch, category = "Routine", isCompleted = false)
        )
        for (item in scheduleList) {
            dao.insertScheduleItem(item)
        }

        // Habits with streaks
        val datesString = "${today.minusDays(4).format(dayFormatter)},${today.minusDays(3).format(dayFormatter)},${today.minusDays(2).format(dayFormatter)},${yesterday.format(dayFormatter)},${today.format(dayFormatter)}"
        val sampleHabits = listOf(
            HabitItem(name = "Daily Exercise", category = "Health", targetDaysPerWeek = 7, currentStreak = 14, bestStreak = 21, completedDatesCsv = datesString, reminderTime = "07:00", iconName = "fitness"),
            HabitItem(name = "Read 20 Pages", category = "Learning", targetDaysPerWeek = 7, currentStreak = 8, bestStreak = 15, completedDatesCsv = datesString, reminderTime = "21:30", iconName = "book"),
            HabitItem(name = "Drink 2.5L Water", category = "Health", targetDaysPerWeek = 7, currentStreak = 22, bestStreak = 30, completedDatesCsv = datesString, reminderTime = "09:00", iconName = "water"),
            HabitItem(name = "Mindful Meditation", category = "Health", targetDaysPerWeek = 5, currentStreak = 5, bestStreak = 12, completedDatesCsv = datesString, reminderTime = "06:45", iconName = "spa"),
            HabitItem(name = "Deep Study / Code", category = "Learning", targetDaysPerWeek = 5, currentStreak = 6, bestStreak = 10, completedDatesCsv = datesString, reminderTime = "14:00", iconName = "code"),
            HabitItem(name = "Sleep On Time (11 PM)", category = "Routine", targetDaysPerWeek = 7, currentStreak = 4, bestStreak = 9, completedDatesCsv = datesString, reminderTime = "22:45", iconName = "bed")
        )
        for (habit in sampleHabits) {
            dao.insertHabit(habit)
        }

        // Sample Focus sessions
        val sampleFocusSessions = listOf(
            FocusSession(durationMinutes = 25, completedAtTimestamp = System.currentTimeMillis() - 1000 * 60 * 180, sessionType = "FOCUS", taskTitle = "Morning Sprint"),
            FocusSession(durationMinutes = 50, completedAtTimestamp = System.currentTimeMillis() - 1000 * 60 * 120, sessionType = "FOCUS", taskTitle = "Architecture Design"),
            FocusSession(durationMinutes = 25, completedAtTimestamp = System.currentTimeMillis() - 1000 * 60 * 60, sessionType = "FOCUS", taskTitle = "Client Prep")
        )
        for (session in sampleFocusSessions) {
            dao.insertFocusSession(session)
        }
    }
}
