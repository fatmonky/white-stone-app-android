package com.whitestone.app.ui.addstone

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.whitestone.app.data.Stone
import com.whitestone.app.data.StoneIntensity
import com.whitestone.app.data.StoneRoot
import com.whitestone.app.data.StoneType
import com.whitestone.app.data.customRootDescriptors
import com.whitestone.app.ui.components.StoneIcon
import com.whitestone.app.ui.components.StoneTagEditor
import com.whitestone.app.data.toRootDescriptorString
import com.whitestone.app.data.toRootTagsCsv
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStoneSheet(
    stoneType: StoneType,
    allStones: List<Stone>,
    onDismiss: () -> Unit,
    onSave: (StoneType, Long, String, String?, String?, String?) -> Unit
) {
    val now = LocalTime.now()
    val timePickerState = rememberTimePickerState(
        initialHour = now.hour,
        initialMinute = now.minute
    )
    var note by remember { mutableStateOf("") }
    var selectedRoots by remember(stoneType) { mutableStateOf<Set<StoneRoot>>(emptySet()) }
    var customDescriptors by remember(stoneType) { mutableStateOf(emptyList<String>()) }
    var selectedIntensity by remember { mutableStateOf<StoneIntensity?>(null) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val reusableCustomDescriptors = remember(allStones, stoneType, customDescriptors) {
        allStones
            .filter { it.type == stoneType }
            .flatMap { it.customRootDescriptors }
            .distinct()
            .filter { it !in customDescriptors }
            .sorted()
    }

    LaunchedEffect(Unit) {
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
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

                Text(
                    text = "Time",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                TimePicker(state = timePickerState)

                Spacer(modifier = Modifier.height(16.dp))

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

                Spacer(modifier = Modifier.height(16.dp))

                StoneTagEditor(
                    stoneType = stoneType,
                    selectedRoots = selectedRoots,
                    onSelectedRootsChange = { selectedRoots = it },
                    customDescriptors = customDescriptors,
                    onCustomDescriptorsChange = { customDescriptors = it },
                    selectedIntensity = selectedIntensity,
                    onSelectedIntensityChange = { selectedIntensity = it },
                    reusableCustomDescriptors = reusableCustomDescriptors
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

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
                        onSave(
                            stoneType,
                            millis,
                            note,
                            selectedRoots.toRootTagsCsv(),
                            customDescriptors.toRootDescriptorString(),
                            selectedIntensity?.rawValue
                        )
                    }
                ) {
                    Text("Save")
                }
            }
        }
    }
}
