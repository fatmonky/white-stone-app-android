package com.whitestone.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingCoachOverlay(
    visible: Boolean,
    onCompleteCoach: () -> Unit,
    onDismissCoach: () -> Unit,
    modifier: Modifier = Modifier
) {
    var coachStep by remember { mutableIntStateOf(0) }

    LaunchedEffect(visible) {
        if (visible) {
            coachStep = 0
        }
    }

    if (!visible) return

    val title = if (coachStep == 0) "Swipe to switch stone" else "Hold to log this stone"
    val body = if (coachStep == 0) {
        "Swipe left or right on the stone to switch between White and Black."
    } else {
        "Press and hold the stone for a moment to open the log sheet."
    }
    val primary = if (coachStep == 0) "Next" else "Try it now"

    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    }
                }
            }
        )

        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = {
                        when (val advance = nextTodayCoachAdvance(coachStep)) {
                            TodayCoachAdvance.Complete -> onCompleteCoach()
                            is TodayCoachAdvance.ShowStep -> coachStep = advance.step
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(primary)
                }
                OutlinedButton(
                    onClick = onDismissCoach,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Not now")
                }
            }
        }
    }
}
