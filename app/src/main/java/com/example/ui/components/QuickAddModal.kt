package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddModal(
    onDismiss: () -> Unit,
    onNewTask: () -> Unit,
    onNewHabit: () -> Unit,
    onAddSchedule: () -> Unit,
    onStartFocus: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp, top = 8.dp)
        ) {
            Text(
                text = "Quick Action",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "What would you like to create?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            QuickAddOptionItem(
                title = "New Task",
                subtitle = "Add an action item with priority & deadline",
                icon = Icons.Default.CheckCircle,
                accentColor = CyanPrimary,
                testTag = "quick_add_task",
                onClick = {
                    onDismiss()
                    onNewTask()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            QuickAddOptionItem(
                title = "New Habit",
                subtitle = "Build daily consistency and track your streaks",
                icon = Icons.Default.Repeat,
                accentColor = AmberAccent,
                testTag = "quick_add_habit",
                onClick = {
                    onDismiss()
                    onNewHabit()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            QuickAddOptionItem(
                title = "Add Schedule Item",
                subtitle = "Block time on your daily timeline planner",
                icon = Icons.Default.CalendarToday,
                accentColor = IndigoSecondary,
                testTag = "quick_add_schedule",
                onClick = {
                    onDismiss()
                    onAddSchedule()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            QuickAddOptionItem(
                title = "Start Focus Timer",
                subtitle = "Launch a Pomodoro or Deep Work session",
                icon = Icons.Default.Timer,
                accentColor = EmeraldSuccess,
                testTag = "quick_add_focus",
                onClick = {
                    onDismiss()
                    onStartFocus()
                }
            )
        }
    }
}

@Composable
private fun QuickAddOptionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
