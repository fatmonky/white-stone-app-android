package com.whitestone.app.ui.trends

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.whitestone.app.data.Stone
import com.whitestone.app.data.StoneType
import com.whitestone.app.ui.components.EmptyStateView
import com.whitestone.app.ui.components.StoneIcon
import com.whitestone.app.ui.components.StoneTimelineItem
import com.whitestone.app.ui.theme.BrownAccent
import com.whitestone.app.util.DateHelpers
import java.time.LocalDate

private data class DayBarData(
    val dayKey: String,
    val label: String,
    val whiteCount: Int,
    val blackCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsScreen(
    onNavigateToStoneDetail: (Long) -> Unit,
    viewModel: TrendsViewModel = hiltViewModel()
) {
    val allStones by viewModel.allStones.collectAsState(initial = emptyList())
    var selectedDayKey by remember { mutableStateOf<String?>(null) }
    var showStonesList by remember { mutableStateOf(false) }

    val totalWhite = allStones.count { it.type == StoneType.WHITE }
    val totalBlack = allStones.count { it.type == StoneType.BLACK }

    // Current streak
    val currentStreak = remember(allStones) {
        var streak = 0
        var date = LocalDate.now()
        while (true) {
            val key = DateHelpers.dayKey(date)
            val dayStones = allStones.filter { it.dayKey == key }
            if (dayStones.isEmpty()) break
            val white = dayStones.count { it.type == StoneType.WHITE }
            if (white * 2 >= dayStones.size) {
                streak++
            } else {
                break
            }
            date = date.minusDays(1)
        }
        streak
    }

    // Daily data for chart (past 14 days)
    val dailyData = remember(allStones) {
        val today = LocalDate.now()
        (13 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val key = DateHelpers.dayKey(date)
            val dayStones = allStones.filter { it.dayKey == key }
            val label = DateHelpers.dayAbbreviation(date) + "\n" + DateHelpers.dayNumber(date)
            DayBarData(
                dayKey = key,
                label = label,
                whiteCount = dayStones.count { it.type == StoneType.WHITE },
                blackCount = dayStones.count { it.type == StoneType.BLACK }
            )
        }
    }

    val selectedStonesForDay = remember(allStones, selectedDayKey) {
        selectedDayKey?.let { key ->
            allStones.filter { it.dayKey == key }.sortedBy { it.timestamp }
        } ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Trends") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(vertical = 16.dp)
        ) {
            // Overview section
            item {
                Text(
                    text = "Overview",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard(
                        title = "Total White",
                        value = "$totalWhite",
                        stoneType = StoneType.WHITE,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Total Black",
                        value = "$totalBlack",
                        stoneType = StoneType.BLACK,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Streak",
                        value = "${currentStreak}d",
                        stoneType = null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Chart section
            item {
                Text(
                    text = "Daily Stones (past 14 days)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            item {
                if (allStones.isEmpty()) {
                    EmptyStateView(
                        message = "Add some stones to see trends.",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                } else {
                    StackedBarChart(
                        data = dailyData,
                        selectedDayKey = selectedDayKey,
                        onBarTapped = { dayKey ->
                            if (selectedDayKey == dayKey) {
                                showStonesList = false
                                selectedDayKey = null
                            } else {
                                showStonesList = false
                                selectedDayKey = dayKey
                                // Delay showing stones list for animation
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    showStonesList = true
                                }, 400)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(horizontal = 16.dp)
                    )
                }
            }

            // Expandable day detail
            if (selectedDayKey != null && selectedStonesForDay.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    AnimatedVisibility(visible = showStonesList, enter = fadeIn()) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val date = DateHelpers.dateFromDayKey(selectedDayKey!!)
                                if (date != null) {
                                    Text(
                                        text = DateHelpers.fullDateString(date),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = {
                                    showStonesList = false
                                    selectedDayKey = null
                                }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                if (showStonesList) {
                    itemsIndexed(selectedStonesForDay, key = { _, stone -> stone.id }) { index, stone ->
                        StoneTimelineItem(
                            stone = stone,
                            isFirst = index == 0,
                            isLast = index == selectedStonesForDay.lastIndex,
                            totalCount = selectedStonesForDay.size,
                            onClick = { onNavigateToStoneDetail(stone.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    stoneType: StoneType?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (stoneType != null) {
            StoneIcon(type = stoneType, size = 28.dp)
        }
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = if (stoneType == null) BrownAccent else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StackedBarChart(
    data: List<DayBarData>,
    selectedDayKey: String?,
    onBarTapped: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val whiteBarColor = Color(0xFFC8C8C8)
    val blackBarColor = Color(0xFF333333)
    val maxTotal = data.maxOfOrNull { it.whiteCount + it.blackCount } ?: 1

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        val barWidth = size.width.toFloat() / data.size
                        val index = (offset.x / barWidth).toInt().coerceIn(0, data.lastIndex)
                        onBarTapped(data[index].dayKey)
                    }
                }
        ) {
            val barCount = data.size
            val spacing = 4.dp.toPx()
            val barWidth = (size.width - spacing * (barCount - 1)) / barCount
            val labelHeight = 32.dp.toPx()
            val chartHeight = size.height - labelHeight

            data.forEachIndexed { index, bar ->
                val x = index * (barWidth + spacing)
                val total = bar.whiteCount + bar.blackCount
                val isSelected = selectedDayKey == null || selectedDayKey == bar.dayKey
                val alpha = if (isSelected) 1f else 0.4f

                if (total > 0) {
                    val totalHeight = (total.toFloat() / maxTotal) * chartHeight
                    val blackHeight = (bar.blackCount.toFloat() / maxTotal) * chartHeight
                    val whiteHeight = totalHeight - blackHeight

                    // Black bar (bottom)
                    drawRoundRect(
                        color = blackBarColor.copy(alpha = alpha),
                        topLeft = Offset(x, chartHeight - totalHeight),
                        size = Size(barWidth, blackHeight),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )

                    // White bar (top of stack)
                    drawRoundRect(
                        color = whiteBarColor.copy(alpha = alpha),
                        topLeft = Offset(x, chartHeight - whiteHeight),
                        size = Size(barWidth, whiteHeight),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                }

                // Labels
                val lines = bar.label.split("\n")
                val paint = android.graphics.Paint().apply {
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    color = android.graphics.Color.GRAY
                }
                val centerX = x + barWidth / 2
                drawContext.canvas.nativeCanvas.apply {
                    if (lines.size >= 2) {
                        drawText(lines[0], centerX, chartHeight + 14.dp.toPx(), paint)
                        drawText(lines[1], centerX, chartHeight + 26.dp.toPx(), paint)
                    }
                }
            }
        }
    }
}
