package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScheduleItem
import com.example.data.model.TaskItem
import com.example.ui.CalendarViewMode
import com.example.ui.components.TaskCard
import com.example.ui.components.TimelineItemRow
import com.example.ui.components.TopNavBar
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoSecondary
import com.example.util.DateUtils
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CalendarScreen(
    tasks: List<TaskItem>,
    scheduleItems: List<ScheduleItem>,
    selectedDate: LocalDate,
    calendarMode: CalendarViewMode,
    onDateSelected: (LocalDate) -> Unit,
    onModeSelected: (CalendarViewMode) -> Unit,
    onToggleTask: (TaskItem) -> Unit,
    onDeleteTask: (TaskItem) -> Unit,
    onToggleSchedule: (ScheduleItem) -> Unit,
    onDeleteSchedule: (ScheduleItem) -> Unit,
    onOpenProfile: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var displayedYearMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }

    val dateEpoch = selectedDate.toEpochDay()
    val dayTasks = tasks.filter { it.dueDateEpochDay == dateEpoch }
    val daySchedule = scheduleItems.filter { it.dateEpochDay == dateEpoch }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_screen_content")
    ) {
        // Header
        item {
            TopNavBar(
                userName = "",
                title = "Calendar",
                subtitle = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())),
                onProfileClick = onOpenProfile,
                onNotificationClick = onNotificationClick
            )
        }

        // View Mode Toggle (Month / Week / Day)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CalendarViewMode.entries.forEach { mode ->
                    val isSelected = calendarMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) CyanPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 0.dp,
                                color = CyanPrimary,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onModeSelected(mode) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${mode.label} View",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) CyanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Calendar Grid / Strip Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Month navigation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayedYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row {
                            IconButton(onClick = { displayedYearMonth = displayedYearMonth.minusMonths(1) }) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                            }
                            IconButton(onClick = { displayedYearMonth = displayedYearMonth.plusMonths(1) }) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Days of week header
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEach { d ->
                            Text(
                                text = d,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Month or Week days
                    val firstDayOfMonth = displayedYearMonth.atDay(1)
                    val daysInMonth = displayedYearMonth.lengthOfMonth()
                    val leadingEmptyDays = (firstDayOfMonth.dayOfWeek.value - 1) // 0 for Mon
                    val totalSlots = ((leadingEmptyDays + daysInMonth + 6) / 7) * 7

                    val rows = totalSlots / 7
                    for (row in 0 until rows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            for (col in 0..6) {
                                val slotIndex = row * 7 + col
                                val dayNumber = slotIndex - leadingEmptyDays + 1

                                if (dayNumber in 1..daysInMonth) {
                                    val date = displayedYearMonth.atDay(dayNumber)
                                    val isSelected = date == selectedDate
                                    val isToday = date == LocalDate.now()
                                    val hasTasks = tasks.any { it.dueDateEpochDay == date.toEpochDay() }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSelected -> CyanPrimary
                                                    isToday -> CyanPrimary.copy(alpha = 0.2f)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .clickable { onDateSelected(date) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = dayNumber.toString(),
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (hasTasks) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(4.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) Color.White else IndigoSecondary)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section header for selected day
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Plans for ${DateUtils.formatShortDate(dateEpoch)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${dayTasks.size} tasks • ${daySchedule.size} blocks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (dayTasks.isEmpty() && daySchedule.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Items for this Day",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enjoy your free time or schedule tasks using the Quick Add button.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Tasks for selected day
        items(dayTasks, key = { "cal_task_${it.id}" }) { task ->
            TaskCard(
                task = task,
                onToggleComplete = { onToggleTask(task) },
                onDelete = { onDeleteTask(task) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        // Schedule blocks for selected day
        items(daySchedule, key = { "cal_sched_${it.id}" }) { item ->
            TimelineItemRow(
                item = item,
                isFirst = false,
                isLast = false,
                onToggleComplete = { onToggleSchedule(item) },
                onDelete = { onDeleteSchedule(item) },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}
