package com.whitestone.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.whitestone.app.data.StoneIntensity
import com.whitestone.app.data.StoneRoot
import com.whitestone.app.data.StoneType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StoneTagEditor(
    stoneType: StoneType,
    selectedRoots: Set<StoneRoot>,
    onSelectedRootsChange: (Set<StoneRoot>) -> Unit,
    customDescriptors: List<String>,
    onCustomDescriptorsChange: (List<String>) -> Unit,
    selectedIntensity: StoneIntensity?,
    onSelectedIntensityChange: (StoneIntensity?) -> Unit,
    reusableCustomDescriptors: List<String>,
    modifier: Modifier = Modifier
) {
    var showCustomInput by remember { mutableStateOf(false) }
    var customInput by remember { mutableStateOf("") }
    val visibleCustomDescriptors = remember(customDescriptors, reusableCustomDescriptors) {
        (customDescriptors + reusableCustomDescriptors).distinct()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Root (optional)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StoneRoot.allowedFor(stoneType).forEach { root ->
                FilterChip(
                    selected = root in selectedRoots,
                    onClick = {
                        onSelectedRootsChange(
                            if (root in selectedRoots) selectedRoots - root else selectedRoots + root
                        )
                    },
                    label = { Text(root.displayName) }
                )
            }

            visibleCustomDescriptors.forEach { descriptor ->
                FilterChip(
                    selected = descriptor in customDescriptors,
                    onClick = {
                        onCustomDescriptorsChange(
                            if (descriptor in customDescriptors) {
                                customDescriptors - descriptor
                            } else {
                                customDescriptors + descriptor
                            }
                        )
                    },
                    label = { Text(descriptor) }
                )
            }

            TextButton(onClick = { showCustomInput = true }) {
                Text("+ custom")
            }
        }

        if (showCustomInput) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = customInput,
                onValueChange = { customInput = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Custom root") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val descriptor = customInput.trim()
                    if (descriptor.isNotEmpty() && descriptor !in customDescriptors) {
                        onCustomDescriptorsChange(customDescriptors + descriptor)
                    }
                    customInput = ""
                    showCustomInput = false
                }
            ) {
                Text("Add custom")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Intensity (optional)",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StoneIntensity.entries.forEach { intensity ->
                FilterChip(
                    selected = selectedIntensity == intensity,
                    onClick = {
                        onSelectedIntensityChange(
                            if (selectedIntensity == intensity) null else intensity
                        )
                    },
                    label = { Text(intensity.displayName.replaceFirstChar { it.uppercase() }) }
                )
            }
        }
    }
}
