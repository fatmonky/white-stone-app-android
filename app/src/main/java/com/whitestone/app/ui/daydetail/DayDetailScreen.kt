package com.whitestone.app.ui.daydetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.whitestone.app.data.StoneType
import com.whitestone.app.ui.components.EmptyStateView
import com.whitestone.app.ui.components.RatioBar
import com.whitestone.app.ui.components.StoneIcon
import com.whitestone.app.ui.components.StoneTimelineItem
import com.whitestone.app.util.DateHelpers

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    dayKey: String,
    onNavigateToStoneDetail: (Long) -> Unit,
    onNavigateToReflectionDetail: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: DayDetailViewModel = hiltViewModel()
) {
    val stones by viewModel.getStonesForDay(dayKey).collectAsState(initial = emptyList())
    val reflection by viewModel.getReflectionForDay(dayKey).collectAsState(initial = null)
    val whiteCount = stones.count { it.type == StoneType.WHITE }
    val blackCount = stones.count { it.type == StoneType.BLACK }
    val dateString = DateHelpers.dateFromDayKey(dayKey)?.let { DateHelpers.fullDateString(it) } ?: dayKey

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(dateString) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Summary
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        StoneIcon(type = StoneType.WHITE, size = 20.dp)
                        Text("$whiteCount", style = MaterialTheme.typography.bodyLarge)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        StoneIcon(type = StoneType.BLACK, size = 20.dp)
                        Text("$blackCount", style = MaterialTheme.typography.bodyLarge)
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    if (whiteCount + blackCount > 0) {
                        RatioBar(
                            white = whiteCount,
                            black = blackCount,
                            modifier = Modifier
                                .width(80.dp)
                                .height(8.dp)
                        )
                    }
                }
            }

            if (stones.isEmpty()) {
                item {
                    if (reflection == null) {
                        EmptyStateView(message = "No stones recorded this day.")
                    }
                }
            } else {
                item {
                    Text(
                        text = "Stones",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                itemsIndexed(stones, key = { _, stone -> stone.id }) { index, stone ->
                    StoneTimelineItem(
                        stone = stone,
                        isFirst = index == 0,
                        isLast = index == stones.lastIndex,
                        totalCount = stones.size,
                        onClick = { onNavigateToStoneDetail(stone.id) }
                    )
                }
            }

            reflection?.let { dayReflection ->
                item {
                    Text(
                        text = "Reflection",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                item {
                    com.whitestone.app.ui.reflection.DayReflectionCard(
                        reflection = dayReflection,
                        onClick = onNavigateToReflectionDetail,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}
