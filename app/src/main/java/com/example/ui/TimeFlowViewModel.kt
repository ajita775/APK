package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.TimeFlowDatabase
import com.example.data.model.FocusSession
import com.example.data.model.HabitItem
import com.example.data.model.Priority
import com.example.data.model.ScheduleItem
import com.example.data.model.TaskCategory
import com.example.data.model.TaskItem
import com.example.data.model.UserProfile
import com.example.data.repository.TimeFlowRepository
import com.example.util.DateUtils
import com.example.util.NotificationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class AppNavTab(val label: String) {
    HOME("Home"),
    TASKS("Tasks"),
    PLANNER("Planner"),
    FOCUS("Focus"),
    HABITS("Habits"),
    INSIGHTS("Insights")
}

enum class TaskFilterMode(val label: String) {
    TODAY("Today"),
    UPCOMING("Upcoming"),
    COMPLETED("Completed"),
    HIGH_PRIORITY("High"),
    ALL("All")
}

enum class CalendarViewMode(val label: String) {
    MONTH("Month"),
    WEEK("Week"),
    DAY("Day")
}

enum class FocusPreset(val label: String, val focusMinutes: Int, val breakMinutes: Int) {
    POMODORO_25("25 / 5 Min", 25, 5),
    EXTENDED_50("50 / 10 Min", 50, 10),
    DEEP_WORK_90("90 / 15 Min", 90, 15),
    CUSTOM("Custom", 30, 5)
}

class TimeFlowViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TimeFlowRepository

    init {
        val dao = TimeFlowDatabase.getInstance(application).timeFlowDao()
        repository = TimeFlowRepository(dao)
        NotificationHelper.initNotificationChannel(application)

        // Seed initial sample data if repository is empty
        viewModelScope.launch {
            repository.seedSampleDataIfNeeded(force = false)
        }
    }

    // Repository flows
    val allTasks: StateFlow<List<TaskItem>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHabits: StateFlow<List<HabitItem>> = repository.allHabits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allScheduleItems: StateFlow<List<ScheduleItem>> = repository.allScheduleItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFocusSessions: StateFlow<List<FocusSession>> = repository.allFocusSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Navigation and UI state
    private val _currentTab = MutableStateFlow(AppNavTab.HOME)
    val currentTab = _currentTab.asStateFlow()

    private val _showSettings = MutableStateFlow(false)
    val showSettings = _showSettings.asStateFlow()

    private val _showQuickAdd = MutableStateFlow(false)
    val showQuickAdd = _showQuickAdd.asStateFlow()

    private val _showOnboarding = MutableStateFlow(false)
    val showOnboarding = _showOnboarding.asStateFlow()

    // Tasks filter & search state
    private val _taskFilter = MutableStateFlow(TaskFilterMode.TODAY)
    val taskFilter = _taskFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<TaskCategory?>(null)
    val selectedCategoryFilter = _selectedCategoryFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Filtered tasks flow
    val filteredTasks: StateFlow<List<TaskItem>> = combine(
        allTasks,
        _taskFilter,
        _selectedCategoryFilter,
        _searchQuery
    ) { tasks, filter, category, query ->
        val todayEpoch = DateUtils.getTodayEpochDay()
        tasks.filter { task ->
            // Filter match
            val matchesFilter = when (filter) {
                TaskFilterMode.TODAY -> task.dueDateEpochDay == todayEpoch
                TaskFilterMode.UPCOMING -> task.dueDateEpochDay >= todayEpoch && !task.isCompleted
                TaskFilterMode.COMPLETED -> task.isCompleted
                TaskFilterMode.HIGH_PRIORITY -> task.priority.equals(Priority.HIGH.name, ignoreCase = true)
                TaskFilterMode.ALL -> true
            }

            // Category match
            val matchesCategory = category == null || task.category.equals(category.name, ignoreCase = true)

            // Search match
            val matchesQuery = query.isBlank() ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.notes.contains(query, ignoreCase = true)

            matchesFilter && matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calendar state
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()

    private val _calendarMode = MutableStateFlow(CalendarViewMode.DAY)
    val calendarMode = _calendarMode.asStateFlow()

    // Focus Timer state
    private val _currentPreset = MutableStateFlow(FocusPreset.POMODORO_25)
    val currentPreset = _currentPreset.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning = _isTimerRunning.asStateFlow()

    private val _isBreakMode = MutableStateFlow(false)
    val isBreakMode = _isBreakMode.asStateFlow()

    private val _timerTotalSeconds = MutableStateFlow(25 * 60)
    val timerTotalSeconds = _timerTotalSeconds.asStateFlow()

    private val _timerRemainingSeconds = MutableStateFlow(25 * 60)
    val timerRemainingSeconds = _timerRemainingSeconds.asStateFlow()

    private val _activeFocusTask = MutableStateFlow("Deep Work")
    val activeFocusTask = _activeFocusTask.asStateFlow()

    private val _focusCompletionCelebration = MutableStateFlow(false)
    val focusCompletionCelebration = _focusCompletionCelebration.asStateFlow()

    private var timerJob: Job? = null

    // Navigation setters
    fun setTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun setShowSettings(show: Boolean) {
        _showSettings.value = show
    }

    fun setShowQuickAdd(show: Boolean) {
        _showQuickAdd.value = show
    }

    fun setShowOnboarding(show: Boolean) {
        _showOnboarding.value = show
    }

    fun setTaskFilter(filter: TaskFilterMode) {
        _taskFilter.value = filter
    }

    fun setSelectedCategoryFilter(category: TaskCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun setCalendarMode(mode: CalendarViewMode) {
        _calendarMode.value = mode
    }

    // Task CRUD actions
    fun createTask(
        title: String,
        notes: String = "",
        priority: Priority = Priority.MEDIUM,
        category: TaskCategory = TaskCategory.WORK,
        dueDate: LocalDate = LocalDate.now(),
        startTime: String = "",
        endTime: String = "",
        reminder: Boolean = false,
        recurring: Boolean = false
    ) {
        viewModelScope.launch {
            val task = TaskItem(
                title = title.trim(),
                notes = notes.trim(),
                priority = priority.name,
                category = category.name,
                dueDateEpochDay = dueDate.toEpochDay(),
                startTime = startTime,
                endTime = endTime,
                reminderEnabled = reminder,
                isRecurring = recurring
            )
            repository.insertTask(task)
            if (reminder) {
                NotificationHelper.showReminderNotification(
                    getApplication(),
                    System.currentTimeMillis().toInt(),
                    "Task Created: $title",
                    "Scheduled for ${DateUtils.formatShortDate(dueDate.toEpochDay())} ${if (startTime.isNotBlank()) "at $startTime" else ""}"
                )
            }
        }
    }

    fun toggleTaskComplete(task: TaskItem) {
        viewModelScope.launch {
            val updated = task.copy(
                isCompleted = !task.isCompleted,
                completedAtTimestamp = if (!task.isCompleted) System.currentTimeMillis() else null
            )
            repository.updateTask(updated)
            if (updated.isCompleted) {
                NotificationHelper.triggerHapticFeedback(getApplication())
            }
        }
    }

    fun updateTask(task: TaskItem) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Habit CRUD & streak logic
    fun createHabit(
        name: String,
        category: String = "Health",
        targetDaysPerWeek: Int = 7,
        reminderTime: String = "08:00",
        iconName: String = "fitness"
    ) {
        viewModelScope.launch {
            val habit = HabitItem(
                name = name.trim(),
                category = category,
                targetDaysPerWeek = targetDaysPerWeek,
                currentStreak = 0,
                bestStreak = 0,
                completedDatesCsv = "",
                reminderTime = reminderTime,
                iconName = iconName
            )
            repository.insertHabit(habit)
        }
    }

    fun toggleHabitToday(habit: HabitItem) {
        viewModelScope.launch {
            val todayStr = DateUtils.toIsoDate(LocalDate.now())
            val dateList = habit.completedDatesCsv.split(",")
                .filter { it.isNotBlank() }
                .toMutableList()

            val isDoneToday = dateList.contains(todayStr)
            val newStreak: Int
            val newBest: Int

            if (isDoneToday) {
                dateList.remove(todayStr)
                newStreak = maxOf(0, habit.currentStreak - 1)
                newBest = habit.bestStreak
            } else {
                dateList.add(todayStr)
                newStreak = habit.currentStreak + 1
                newBest = maxOf(habit.bestStreak, newStreak)
                NotificationHelper.triggerHapticFeedback(getApplication())
            }

            val updated = habit.copy(
                completedDatesCsv = dateList.joinToString(","),
                currentStreak = newStreak,
                bestStreak = newBest
            )
            repository.updateHabit(updated)
        }
    }

    fun deleteHabit(habit: HabitItem) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    // Schedule items CRUD
    fun createScheduleItem(
        title: String,
        startTime: String,
        endTime: String,
        date: LocalDate = LocalDate.now(),
        category: String = "Work"
    ) {
        viewModelScope.launch {
            val item = ScheduleItem(
                title = title.trim(),
                startTime = startTime,
                endTime = endTime,
                dateEpochDay = date.toEpochDay(),
                category = category,
                isCompleted = false
            )
            repository.insertScheduleItem(item)
        }
    }

    fun toggleScheduleItemComplete(item: ScheduleItem) {
        viewModelScope.launch {
            repository.updateScheduleItem(item.copy(isCompleted = !item.isCompleted))
            NotificationHelper.triggerHapticFeedback(getApplication())
        }
    }

    fun deleteScheduleItem(item: ScheduleItem) {
        viewModelScope.launch {
            repository.deleteScheduleItem(item)
        }
    }

    // Focus Timer Engine
    fun setFocusPreset(preset: FocusPreset) {
        _currentPreset.value = preset
        val durationMinutes = if (_isBreakMode.value) preset.breakMinutes else preset.focusMinutes
        _timerTotalSeconds.value = durationMinutes * 60
        _timerRemainingSeconds.value = durationMinutes * 60
        stopTimer()
    }

    fun setCustomDurationMinutes(minutes: Int) {
        _currentPreset.value = FocusPreset.CUSTOM
        _timerTotalSeconds.value = minutes * 60
        _timerRemainingSeconds.value = minutes * 60
        stopTimer()
    }

    fun setActiveFocusTask(taskTitle: String) {
        _activeFocusTask.value = taskTitle
    }

    fun startTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        _focusCompletionCelebration.value = false

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value && _timerRemainingSeconds.value > 0) {
                delay(1000)
                _timerRemainingSeconds.value -= 1
            }

            if (_timerRemainingSeconds.value <= 0 && _isTimerRunning.value) {
                _isTimerRunning.value = false
                NotificationHelper.triggerHapticFeedback(getApplication())

                val duration = _timerTotalSeconds.value / 60
                val mode = if (_isBreakMode.value) "BREAK" else "FOCUS"

                // Save session in database
                repository.insertFocusSession(
                    FocusSession(
                        durationMinutes = duration,
                        sessionType = mode,
                        taskTitle = _activeFocusTask.value
                    )
                )

                // Show notification
                NotificationHelper.showReminderNotification(
                    getApplication(),
                    777,
                    if (_isBreakMode.value) "Break Completed!" else "Focus Session Complete! 🎉",
                    if (_isBreakMode.value) "Ready to jump back into productive focus?" else "Great job! You focused for $duration minutes on ${_activeFocusTask.value}."
                )

                _focusCompletionCelebration.value = true

                // Auto switch between Focus and Break
                _isBreakMode.value = !_isBreakMode.value
                val nextDuration = if (_isBreakMode.value) _currentPreset.value.breakMinutes else _currentPreset.value.focusMinutes
                _timerTotalSeconds.value = nextDuration * 60
                _timerRemainingSeconds.value = nextDuration * 60
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resumeTimer() {
        startTimer()
    }

    fun stopTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        _focusCompletionCelebration.value = false
        val durationMinutes = if (_isBreakMode.value) _currentPreset.value.breakMinutes else _currentPreset.value.focusMinutes
        _timerRemainingSeconds.value = durationMinutes * 60
    }

    fun resetTimer() {
        stopTimer()
        _isBreakMode.value = false
        _timerTotalSeconds.value = _currentPreset.value.focusMinutes * 60
        _timerRemainingSeconds.value = _currentPreset.value.focusMinutes * 60
    }

    fun dismissCelebration() {
        _focusCompletionCelebration.value = false
    }

    // Profile updates
    fun saveUserProfile(
        name: String,
        wakeUpTime: String,
        sleepTime: String,
        goal: String,
        themeMode: String = "SYSTEM",
        haptics: Boolean = true,
        defaultFocus: Int = 25,
        defaultBreak: Int = 5,
        completedOnboarding: Boolean = true
    ) {
        viewModelScope.launch {
            val profile = UserProfile(
                id = 1,
                name = name.ifBlank { "Ajeet" },
                wakeUpTime = wakeUpTime.ifBlank { "06:30" },
                sleepTime = sleepTime.ifBlank { "23:00" },
                productivityGoal = goal.ifBlank { "Master Daily Time & Focus" },
                isOnboardingCompleted = completedOnboarding,
                themeMode = themeMode,
                isHapticsEnabled = haptics,
                defaultFocusDuration = defaultFocus,
                defaultBreakDuration = defaultBreak
            )
            repository.saveUserProfile(profile)
        }
    }

    fun resetToSampleData() {
        viewModelScope.launch {
            repository.seedSampleDataIfNeeded(force = true)
        }
    }

    fun triggerTestNotification() {
        NotificationHelper.showReminderNotification(
            getApplication(),
            101,
            "TimeFlow Reminder",
            "Next up: Deep Work Session starts in 15 minutes!"
        )
    }
}
