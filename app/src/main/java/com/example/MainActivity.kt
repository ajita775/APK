package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Priority
import com.example.data.model.TaskCategory
import com.example.ui.AppNavTab
import com.example.ui.CalendarViewMode
import com.example.ui.TimeFlowViewModel
import com.example.ui.components.AddEditTaskDialog
import com.example.ui.components.AddHabitDialog
import com.example.ui.components.AddScheduleItemDialog
import com.example.ui.components.QuickAddModal
import com.example.ui.components.TimeFlowBottomBar
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.FocusScreen
import com.example.ui.screens.HabitsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PlannerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.TimeFlowTheme
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: TimeFlowViewModel = viewModel()
            val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

            // Notification permission request for Android 13+
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { /* handled gracefully */ }
            )

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            // Theme determination (SYSTEM, LIGHT, DARK)
            val isDarkTheme = when (userProfile?.themeMode?.uppercase()) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            TimeFlowTheme(darkTheme = isDarkTheme) {
                TimeFlowApp(
                    viewModel = viewModel,
                    onRequestNotificationPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.triggerTestNotification()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun TimeFlowApp(
    viewModel: TimeFlowViewModel,
    onRequestNotificationPermission: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val allTasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val filteredTasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val allHabits by viewModel.allHabits.collectAsStateWithLifecycle()
    val allScheduleItems by viewModel.allScheduleItems.collectAsStateWithLifecycle()
    val focusSessions by viewModel.allFocusSessions.collectAsStateWithLifecycle()

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val showSettings by viewModel.showSettings.collectAsStateWithLifecycle()
    val showQuickAdd by viewModel.showQuickAdd.collectAsStateWithLifecycle()
    val showOnboarding by viewModel.showOnboarding.collectAsStateWithLifecycle()

    val taskFilter by viewModel.taskFilter.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val calendarMode by viewModel.calendarMode.collectAsStateWithLifecycle()

    // Timer states
    val currentPreset by viewModel.currentPreset.collectAsStateWithLifecycle()
    val timerTotalSeconds by viewModel.timerTotalSeconds.collectAsStateWithLifecycle()
    val timerRemainingSeconds by viewModel.timerRemainingSeconds.collectAsStateWithLifecycle()
    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val isBreakMode by viewModel.isBreakMode.collectAsStateWithLifecycle()
    val activeFocusTask by viewModel.activeFocusTask.collectAsStateWithLifecycle()
    val focusCelebration by viewModel.focusCompletionCelebration.collectAsStateWithLifecycle()

    // Dialog flags
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var showAddScheduleDialog by remember { mutableStateOf(false) }

    // Handle system back navigation
    BackHandler(enabled = showSettings || currentTab != AppNavTab.HOME) {
        if (showSettings) {
            viewModel.setShowSettings(false)
        } else if (currentTab != AppNavTab.HOME) {
            viewModel.setTab(AppNavTab.HOME)
        }
    }

    val needsOnboarding = (userProfile != null && !userProfile!!.isOnboardingCompleted) || showOnboarding

    if (needsOnboarding) {
        OnboardingScreen(
            initialName = userProfile?.name ?: "Ajeet",
            initialWake = userProfile?.wakeUpTime ?: "06:30",
            initialSleep = userProfile?.sleepTime ?: "23:00",
            initialGoal = userProfile?.productivityGoal ?: "Master Daily Time & Focus",
            onCompleteOnboarding = { name, wake, sleep, goal ->
                viewModel.saveUserProfile(
                    name = name,
                    wakeUpTime = wake,
                    sleepTime = sleep,
                    goal = goal,
                    completedOnboarding = true
                )
                viewModel.setShowOnboarding(false)
            }
        )
    } else if (showSettings) {
        SettingsScreen(
            userProfile = userProfile,
            onSaveProfile = { name, wake, sleep, goal, theme, haptics, focus, breakDur ->
                viewModel.saveUserProfile(
                    name = name,
                    wakeUpTime = wake,
                    sleepTime = sleep,
                    goal = goal,
                    themeMode = theme,
                    haptics = haptics,
                    defaultFocus = focus,
                    defaultBreak = breakDur,
                    completedOnboarding = true
                )
            },
            onTestNotification = { viewModel.triggerTestNotification() },
            onResetToSampleData = { viewModel.resetToSampleData() },
            onBack = { viewModel.setShowSettings(false) }
        )
    } else {
        Scaffold(
            bottomBar = {
                TimeFlowBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.setTab(it) }
                )
            },
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Crossfade(targetState = currentTab, label = "screen_fade") { tab ->
                    when (tab) {
                        AppNavTab.HOME -> HomeScreen(
                            userProfile = userProfile,
                            tasks = allTasks,
                            scheduleItems = allScheduleItems,
                            onToggleTask = { viewModel.toggleTaskComplete(it) },
                            onDeleteTask = { viewModel.deleteTask(it) },
                            onStartFocus = {
                                viewModel.setTab(AppNavTab.FOCUS)
                                viewModel.startTimer()
                            },
                            onQuickAdd = { viewModel.setShowQuickAdd(true) },
                            onNavigateToTasks = { viewModel.setTab(AppNavTab.TASKS) },
                            onNavigateToPlanner = { viewModel.setTab(AppNavTab.PLANNER) },
                            onOpenProfile = { viewModel.setShowSettings(true) },
                            onNotificationClick = onRequestNotificationPermission
                        )

                        AppNavTab.TASKS -> TasksScreen(
                            tasks = filteredTasks,
                            currentFilter = taskFilter,
                            selectedCategory = selectedCategory,
                            searchQuery = searchQuery,
                            onFilterSelected = { viewModel.setTaskFilter(it) },
                            onCategorySelected = { viewModel.setSelectedCategoryFilter(it) },
                            onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                            onToggleTask = { viewModel.toggleTaskComplete(it) },
                            onDeleteTask = { viewModel.deleteTask(it) },
                            onAddTask = { showAddTaskDialog = true },
                            onOpenProfile = { viewModel.setShowSettings(true) },
                            onNotificationClick = onRequestNotificationPermission
                        )

                        AppNavTab.PLANNER -> PlannerScreen(
                            scheduleItems = allScheduleItems.filter { it.dateEpochDay == selectedDate.toEpochDay() },
                            selectedDate = selectedDate,
                            onSelectDate = { viewModel.setSelectedDate(it) },
                            onToggleSchedule = { viewModel.toggleScheduleItemComplete(it) },
                            onDeleteSchedule = { viewModel.deleteScheduleItem(it) },
                            onAddScheduleItem = { showAddScheduleDialog = true },
                            onOpenProfile = { viewModel.setShowSettings(true) },
                            onNotificationClick = onRequestNotificationPermission
                        )

                        AppNavTab.FOCUS -> FocusScreen(
                            currentPreset = currentPreset,
                            totalSeconds = timerTotalSeconds,
                            remainingSeconds = timerRemainingSeconds,
                            isRunning = isTimerRunning,
                            isBreakMode = isBreakMode,
                            activeTask = activeFocusTask,
                            focusCelebration = focusCelebration,
                            completedSessions = focusSessions,
                            onSelectPreset = { viewModel.setFocusPreset(it) },
                            onStart = { viewModel.startTimer() },
                            onPause = { viewModel.pauseTimer() },
                            onResume = { viewModel.resumeTimer() },
                            onStop = { viewModel.stopTimer() },
                            onReset = { viewModel.resetTimer() },
                            onDismissCelebration = { viewModel.dismissCelebration() },
                            onOpenProfile = { viewModel.setShowSettings(true) },
                            onNotificationClick = onRequestNotificationPermission
                        )

                        AppNavTab.HABITS -> HabitsScreen(
                            habits = allHabits,
                            onToggleHabitToday = { viewModel.toggleHabitToday(it) },
                            onDeleteHabit = { viewModel.deleteHabit(it) },
                            onAddHabit = { showAddHabitDialog = true },
                            onOpenProfile = { viewModel.setShowSettings(true) },
                            onNotificationClick = onRequestNotificationPermission
                        )

                        AppNavTab.INSIGHTS -> AnalyticsScreen(
                            tasks = allTasks,
                            habits = allHabits,
                            focusSessions = focusSessions,
                            onOpenProfile = { viewModel.setShowSettings(true) },
                            onNotificationClick = onRequestNotificationPermission
                        )
                    }
                }
            }
        }
    }

    // Quick Add Modal Bottom Sheet
    if (showQuickAdd) {
        QuickAddModal(
            onDismiss = { viewModel.setShowQuickAdd(false) },
            onNewTask = { showAddTaskDialog = true },
            onNewHabit = { showAddHabitDialog = true },
            onAddSchedule = { showAddScheduleDialog = true },
            onStartFocus = {
                viewModel.setTab(AppNavTab.FOCUS)
                viewModel.startTimer()
            }
        )
    }

    // Dialogs
    if (showAddTaskDialog) {
        AddEditTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onSaveTask = { title, notes, priority, category, dueDate, startTime, endTime, reminder, recurring ->
                viewModel.createTask(
                    title = title,
                    notes = notes,
                    priority = priority,
                    category = category,
                    dueDate = dueDate,
                    startTime = startTime,
                    endTime = endTime,
                    reminder = reminder,
                    recurring = recurring
                )
            }
        )
    }

    if (showAddHabitDialog) {
        AddHabitDialog(
            onDismiss = { showAddHabitDialog = false },
            onSaveHabit = { name, category, targetDays, reminderTime ->
                viewModel.createHabit(
                    name = name,
                    category = category,
                    targetDaysPerWeek = targetDays,
                    reminderTime = reminderTime
                )
            }
        )
    }

    if (showAddScheduleDialog) {
        AddScheduleItemDialog(
            onDismiss = { showAddScheduleDialog = false },
            onSaveSchedule = { title, startTime, endTime, category ->
                viewModel.createScheduleItem(
                    title = title,
                    startTime = startTime,
                    endTime = endTime,
                    date = selectedDate,
                    category = category
                )
            }
        )
    }
}
