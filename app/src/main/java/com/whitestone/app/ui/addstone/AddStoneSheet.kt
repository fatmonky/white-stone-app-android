package com.whitestone.app.ui.addstone

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.whitestone.app.data.StoneType
import com.whitestone.app.ui.components.StoneIcon
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStoneSheet(
    stoneType: StoneType,
    onDismiss: () -> Unit,
    onSave: (StoneType, Long, String) -> Unit
) {
    val now = LocalTime.now()
    val timePickerState = rememberTimePickerState(
        initialHour = now.hour,
        initialMinute = now.minute
    )
    var note by remember { mutableStateOf("") }
    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StoneIcon(type = stoneType, size = 32.dp)
                Text(
                    text = if (stoneType == StoneType.WHITE) "White Stone" else "Black Stone",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Time picker
            Text(
                text = "Time",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            TimePicker(state = timePickerState)

            Spacer(modifier = Modifier.height(16.dp))

            // Note
            Text(
                text = "Note (optional)",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { Text("What happened?") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        // Haptic feedback
                        val vibrator = context.getSystemService(Vibrator::class.java)
                        vibrator?.vibrate(
                            VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                        )

                        // Build timestamp from selected time + today's date
                        val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        val dateTime = LocalDateTime.of(
                            java.time.LocalDate.now(),
                            selectedTime
                        )
                        val millis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        onSave(stoneType, millis, note)
                    }
                ) {
                    Text("Save")
                }
            }
        }
    }
}
