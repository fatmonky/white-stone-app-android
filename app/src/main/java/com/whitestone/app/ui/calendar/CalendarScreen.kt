package com.whitestone.app.ui.calendar

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.whitestone.app.data.StoneType
import com.whitestone.app.ui.components.EmptyStateView
import com.whitestone.app.ui.components.RatioBar
import com.whitestone.app.ui.components.StoneIcon
import com.whitestone.app.ui.components.StoneTimelineItem
import com.whitestone.app.ui.theme.BrownAccent
import com.whitestone.app.util.ColorHelpers
import com.whitestone.app.util.DateHelpers
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToStoneDetail: (Long) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val allStones by viewModel.allStones.collectAsState(initial = emptyList())
    var displayedMonth by remember { mutableStateOf(LocalDate.now()) }
    var selectedDayKey by remember { mutableStateOf<String?>(DateHelpers.todayKey) }

    val weekdaySymbols = listOf("M", "T", "W", "T", "F", "S", "S")

    // Compute ratio by day
    val ratioByDay = remember(allStones) {
        allStones.groupBy { it.dayKey }.mapValues { (_, stones) ->
            ColorHelpers.ratio(
                white = stones.count { it.type == StoneType.WHITE },
                total = stones.size
            )
        }
    }

    val daysInMonth = DateHelpers.daysInMonth(displayedMonth)
    val weekdayOffset = DateHelpers.weekdayOfFirst(displayedMonth)

    val selectedStones = remember(allStones, selectedDayKey) {
        selectedDayKey?.let { key ->
            allStones.filter { it.dayKey == key }.sortedBy { it.timestamp }
        } ?: emptyList()
    }
    val selectedWhiteCount = selectedStones.count { it.type == StoneType.WHITE }
    val selectedBlackCount = selectedStones.count { it.type == StoneType.BLACK }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Calendar") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Month navigation
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { displayedMonth = DateHelpers.offsetMonth(displayedMonth, -1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = DateHelpers.monthYearString(displayedMonth),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { displayedMonth = DateHelpers.offsetMonth(displayedMonth, 1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
                    }
                }
            }

            // Weekday headers
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    weekdaySymbols.forEach { symbol ->
                        Text(
                            text = symbol,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Calendar grid (rendered as rows of 7)
            val totalCells = weekdayOffset + daysInMonth
            val rows = (totalCells + 6) / 7
            for (row in 0 until rows) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val day = cellIndex - weekdayOffset + 1
                            if (day in 1..daysInMonth) {
                                val key = DateHelpers.dayKeyForDay(day, displayedMonth)
                                val ratio = ratioByDay[key]
                                val isSelected = selectedDayKey == key
                                DayCell(
                                    day = day,
                                    ratio = ratio,
                                    isSelected = isSelected,
                                    onClick = { selectedDayKey = key },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            // Selected day stones
            if (selectedDayKey != null) {
                if (selectedStones.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                StoneIcon(type = StoneType.WHITE, size = 16.dp)
                                Text("$selectedWhiteCount", style = MaterialTheme.typography.bodyMedium)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                StoneIcon(type = StoneType.BLACK, size = 16.dp)
                                Text("$selectedBlackCount", style = MaterialTheme.typography.bodyMedium)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            RatioBar(
                                white = selectedWhiteCount,
                                black = selectedBlackCount,
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(8.dp)
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Stones",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                if (selectedStones.isEmpty()) {
                    item {
                        Text(
                            text = "No stones recorded this day.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    itemsIndexed(selectedStones, key = { _, stone -> stone.id }) { index, stone ->
                        StoneTimelineItem(
                            stone = stone,
                            isFirst = index == 0,
                            isLast = index == selectedStones.lastIndex,
                            totalCount = selectedStones.size,
                            onClick = { onNavigateToStoneDetail(stone.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    ratio: Double?,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = ColorHelpers.colorForRatio(ratio)
    val textColor = if (ratio != null) {
        if (ratio > 0.5) Color.Black else Color.White
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .then(
                if (isSelected) Modifier.border(2.dp, BrownAccent, RoundedCornerShape(6.dp))
                else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$day",
            style = MaterialTheme.typography.bodySmall,
            color = textColor
        )
    }
}
