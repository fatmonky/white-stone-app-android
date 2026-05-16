package com.whitestone.app.ui.stonedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.whitestone.app.data.StoneIntensity
import com.whitestone.app.data.StoneRoot
import com.whitestone.app.data.customRootDescriptors
import com.whitestone.app.data.roots
import com.whitestone.app.data.stoneIntensity
import com.whitestone.app.data.tagSummaryText
import com.whitestone.app.data.toRootDescriptorString
import com.whitestone.app.data.toRootTagsCsv
import com.whitestone.app.data.StoneType
import com.whitestone.app.ui.components.StoneIcon
import com.whitestone.app.ui.components.StoneTagEditor
import com.whitestone.app.util.DateHelpers
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoneDetailScreen(
    stoneId: Long,
    onNavigateBack: () -> Unit,
    viewModel: StoneDetailViewModel = hiltViewModel()
) {
    val stone by viewModel.stone.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var editedNote by remember { mutableStateOf("") }
    var editedTimestamp by remember { mutableStateOf(0L) }
    var editedRoots by remember { mutableStateOf<Set<StoneRoot>>(emptySet()) }
    var editedCustomDescriptors by remember { mutableStateOf(emptyList<String>()) }
    var editedIntensity by remember { mutableStateOf<StoneIntensity?>(null) }

    LaunchedEffect(stoneId) {
        viewModel.loadStone(stoneId)
    }

    val currentStone = stone
    if (currentStone == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Stone Detail") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Stone not found")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stone Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isEditing) {
                        TextButton(onClick = {
                            isEditing = false
                            editedNote = currentStone.note
                            editedTimestamp = currentStone.timestamp
                            editedRoots = currentStone.roots.toSet()
                            editedCustomDescriptors = currentStone.customRootDescriptors
                            editedIntensity = currentStone.stoneIntensity
                        }) { Text("Cancel") }
                        TextButton(onClick = {
                            viewModel.updateStone(
                                currentStone.copy(
                                    note = editedNote,
                                    timestamp = editedTimestamp,
                                    dayKey = DateHelpers.dayKey(editedTimestamp),
                                    rootTagsCsv = editedRoots.toRootTagsCsv(),
                                    rootDescriptor = editedCustomDescriptors.toRootDescriptorString(),
                                    intensity = editedIntensity?.rawValue
                                )
                            )
                            isEditing = false
                        }) { Text("Save") }
                    } else {
                        TextButton(onClick = {
                            editedNote = currentStone.note
                            editedTimestamp = currentStone.timestamp
                            editedRoots = currentStone.roots.toSet()
                            editedCustomDescriptors = currentStone.customRootDescriptors
                            editedIntensity = currentStone.stoneIntensity
                            isEditing = true
                        }) { Text("Edit") }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Stone type header
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StoneIcon(type = currentStone.type, size = 40.dp)
                    Text(
                        text = if (currentStone.type == StoneType.WHITE) "White Stone" else "Black Stone",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isEditing && currentStone.tagSummaryText != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tags",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentStone.tagSummaryText.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Time section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Time",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isEditing) {
                        val dt = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(editedTimestamp),
                            ZoneId.systemDefault()
                        )
                        val timePickerState = rememberTimePickerState(
                            initialHour = dt.hour,
                            initialMinute = dt.minute
                        )
                        var showDatePicker by remember { mutableStateOf(false) }

                        TextButton(onClick = { showDatePicker = true }) {
                            Text(DateHelpers.fullDateString(editedTimestamp))
                        }

                        TimePicker(state = timePickerState)

                        // Update editedTimestamp when time changes
                        LaunchedEffect(timePickerState.hour, timePickerState.minute) {
                            val date = dt.toLocalDate()
                            val newTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                            val newDt = LocalDateTime.of(date, newTime)
                            editedTimestamp = newDt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        }

                        if (showDatePicker) {
                            val datePickerState = rememberDatePickerState(
                                initialSelectedDateMillis = editedTimestamp
                            )
                            DatePickerDialog(
                                onDismissRequest = { showDatePicker = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        datePickerState.selectedDateMillis?.let { millis ->
                                            val selectedDate = Instant.ofEpochMilli(millis)
                                                .atZone(ZoneId.systemDefault())
                                                .toLocalDate()
                                            val currentTime = LocalDateTime.ofInstant(
                                                Instant.ofEpochMilli(editedTimestamp),
                                                ZoneId.systemDefault()
                                            ).toLocalTime()
                                            val newDt = LocalDateTime.of(selectedDate, currentTime)
                                            editedTimestamp = newDt.atZone(ZoneId.systemDefault())
                                                .toInstant().toEpochMilli()
                                        }
                                        showDatePicker = false
                                    }) { Text("OK") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDatePicker = false }) {
                                        Text("Cancel")
                                    }
                                }
                            ) {
                                DatePicker(state = datePickerState)
                            }
                        }
                    } else {
                        Text(
                            text = DateHelpers.fullDateString(currentStone.timestamp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = DateHelpers.timeString(currentStone.timestamp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Note section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Note",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isEditing) {
                        OutlinedTextField(
                            value = editedNote,
                            onValueChange = { editedNote = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            placeholder = { Text("What happened?") }
                        )
                    } else if (currentStone.note.isNotEmpty()) {
                        Text(
                            text = currentStone.note,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        Text(
                            text = "No note",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (isEditing) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        StoneTagEditor(
                            stoneType = currentStone.type,
                            selectedRoots = editedRoots,
                            onSelectedRootsChange = { editedRoots = it },
                            customDescriptors = editedCustomDescriptors,
                            onCustomDescriptorsChange = { editedCustomDescriptors = it },
                            selectedIntensity = editedIntensity,
                            onSelectedIntensityChange = { editedIntensity = it },
                            reusableCustomDescriptors = currentStone.customRootDescriptors
                                .filter { it !in editedCustomDescriptors }
                        )
                    }
                }
            }
        }
    }
}
