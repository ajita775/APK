package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoSecondary

@Composable
fun OnboardingScreen(
    initialName: String,
    initialWake: String,
    initialSleep: String,
    initialGoal: String,
    onCompleteOnboarding: (name: String, wake: String, sleep: String, goal: String) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf(initialName.ifBlank { "Ajeet" }) }
    var wakeUp by remember { mutableStateOf(initialWake.ifBlank { "06:30" }) }
    var sleep by remember { mutableStateOf(initialSleep.ifBlank { "23:00" }) }
    var goal by remember { mutableStateOf(initialGoal.ifBlank { "Master Daily Time & Focus" }) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 0..3) {
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (i == currentStep) 28.dp else 10.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (i <= currentStep) CyanPrimary else MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                    }
                }

                if (currentStep < 3) {
                    TextButton(onClick = { currentStep = 3 }) {
                        Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Spacer(modifier = Modifier.width(40.dp))
                }
            }

            // Step Content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "onboarding_steps"
            ) { step ->
                when (step) {
                    0 -> OnboardingStepCard(
                        title = "Take Control of Your Time",
                        description = "Transform chaotic days into calm, focused progress. Master every hour with TimeFlow's streamlined daily system.",
                        icon = Icons.Default.AccessTime,
                        iconColor = CyanPrimary
                    )
                    1 -> OnboardingStepCard(
                        title = "Plan, Focus & Build Routines",
                        description = "Organize structured tasks, visually map your entire day, and cultivate habits that compound into lasting success.",
                        icon = Icons.Default.Timeline,
                        iconColor = IndigoSecondary
                    )
                    2 -> OnboardingStepCard(
                        title = "Smart Reminders & Insights",
                        description = "Stay accountable with timely alerts and actionable productivity analytics that celebrate your streaks.",
                        icon = Icons.Default.NotificationsActive,
                        iconColor = AmberAccent
                    )
                    3 -> OnboardingProfileStep(
                        name = name,
                        onNameChange = { name = it },
                        wakeUp = wakeUp,
                        onWakeUpChange = { wakeUp = it },
                        sleep = sleep,
                        onSleepChange = { sleep = it },
                        goal = goal,
                        onGoalChange = { goal = it }
                    )
                }
            }

            // Bottom Navigation Controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Button(
                    onClick = {
                        if (currentStep < 3) {
                            currentStep += 1
                        } else {
                            onCompleteOnboarding(name, wakeUp, sleep, goal)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("onboarding_continue_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanPrimary
                    )
                ) {
                    Text(
                        text = if (currentStep < 3) "Continue" else "Get Started",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingStepCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(iconColor.copy(alpha = 0.2f), iconColor.copy(alpha = 0.05f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 24.sp
            ),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OnboardingProfileStep(
    name: String,
    onNameChange: (String) -> Unit,
    wakeUp: String,
    onWakeUpChange: (String) -> Unit,
    sleep: String,
    onSleepChange: (String) -> Unit,
    goal: String,
    onGoalChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = "Personalize TimeFlow",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Tell us about your daily rhythm and primary focus.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Your Name") },
            placeholder = { Text("e.g. Ajeet") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_name_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = wakeUp,
                onValueChange = onWakeUpChange,
                label = { Text("Wake-up Time") },
                placeholder = { Text("06:30") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = sleep,
                onValueChange = onSleepChange,
                label = { Text("Sleep Time") },
                placeholder = { Text("23:00") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = goal,
            onValueChange = onGoalChange,
            label = { Text("Main Productivity Goal") },
            placeholder = { Text("e.g. Master Deep Work & Build Health Routines") },
            maxLines = 2,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
    }
}
