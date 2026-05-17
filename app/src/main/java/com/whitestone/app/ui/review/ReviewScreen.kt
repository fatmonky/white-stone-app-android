package com.whitestone.app.ui.review

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.whitestone.app.data.Stone
import com.whitestone.app.data.StoneType
import com.whitestone.app.ui.components.EmptyStateView
import com.whitestone.app.ui.components.RatioBar
import com.whitestone.app.ui.components.StoneIcon
import com.whitestone.app.ui.components.StoneTimelineItem
import com.whitestone.app.ui.reflection.DayReflectionCard
import com.whitestone.app.ui.theme.BrownAccent
import com.whitestone.app.ui.theme.LightGray
import com.whitestone.app.util.ColorHelpers
import com.whitestone.app.util.DateHelpers
import java.time.LocalDate
import java.time.YearMonth

private data class DayBarData(
    val dayKey: String,
    val label: String,
    val whiteCount: Int,
    val blackCount: Int
)

private data class MonthBarData(
    val label: String,
    val whiteCount: Int,
    val blackCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    onNavigateToStoneDetail: (Long) -> Unit,
    onNavigateToReflectionDetail: (String) -> Unit,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val allStones by viewModel.allStones.collectAsState(initial = emptyList())
    val reflectionDayKeys by viewModel.reflectionDayKeys.collectAsState(initial = emptyList())
    val allReflections by viewModel.allReflections.collectAsState(initial = emptyList())
    var displayedMonth by remember { mutableStateOf(LocalDate.now()) }
    var selectedDayKey by remember { mutableStateOf<String?>(DateHelpers.todayKey) }
    var selectedSecondaryTab by remember { mutableIntStateOf(0) }
    var selectedChartDayKey by remember { mutableStateOf<String?>(null) }
    var showChartStones by remember { mutableStateOf(false) }

    val monthStones = remember(allStones, displayedMonth) {
        allStones.filter {
            val date = DateHelpers.dateFromDayKey(it.dayKey)
            date?.year == displayedMonth.year && date.month == displayedMonth.month
        }
    }
    val monthWhiteCount = monthStones.count { it.type == StoneType.WHITE }
    val monthBlackCount = monthStones.count { it.type == StoneType.BLACK }
    val totalDaysTracked = remember(allStones) { allStones.map { it.dayKey }.distinct().size }
    val currentStreak = remember(allStones) { currentStreak(allStones) }

    val ratioByDay = remember(allStones) {
        allStones.groupBy { it.dayKey }.mapValues { (_, stones) ->
            ColorHelpers.ratio(
                white = stones.count { it.type == StoneType.WHITE },
                total = stones.size
            )
        }
    }

    val selectedStones = remember(allStones, selectedDayKey) {
        selectedDayKey?.let { key ->
            allStones.filter { it.dayKey == key }.sortedBy { it.timestamp }
        } ?: emptyList()
    }
    val selectedReflection = remember(allReflections, selectedDayKey) {
        selectedDayKey?.let { key -> allReflections.firstOrNull { it.dayKey == key } }
    }

    val dailyData = remember(allStones) {
        val today = LocalDate.now()
        (13 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val key = DateHelpers.dayKey(date)
            val dayStones = allStones.filter { it.dayKey == key }
            DayBarData(
                dayKey = key,
                label = DateHelpers.dayAbbreviation(date) + "\n" + DateHelpers.dayNumber(date),
                whiteCount = dayStones.count { it.type == StoneType.WHITE },
                blackCount = dayStones.count { it.type == StoneType.BLACK }
            )
        }
    }

    val monthlyData = remember(allStones) { allTimeMonthlyData(allStones) }
    val selectedChartStones = remember(allStones, selectedChartDayKey) {
        selectedChartDayKey?.let { key ->
            allStones.filter { it.dayKey == key }.sortedBy { it.timestamp }
        } ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Review") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(vertical = 8.dp)
        ) {
            item {
                StatStrip(
                    totalDaysTracked = totalDaysTracked,
                    monthWhiteCount = monthWhiteCount,
                    monthBlackCount = monthBlackCount,
                    currentStreak = currentStreak
                )
            }

            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedSecondaryTab,
                    edgePadding = 16.dp
                ) {
                    listOf("14 days", "All-time", "Patterns").forEachIndexed { index, label ->
                        Tab(
                            selected = selectedSecondaryTab == index,
                            onClick = {
                                selectedSecondaryTab = index
                                selectedChartDayKey = null
                                showChartStones = false
                            },
                            text = { Text(label) }
                        )
                    }
                }
            }

            when (selectedSecondaryTab) {
                0 -> {
                    item {
                        SectionTitle("Daily Stones (past 14 days)")
                        if (allStones.isEmpty()) {
                            EmptyStateView(
                                message = "Add some stones to see trends.",
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        } else {
                            StackedBarChart(
                                data = dailyData,
                                selectedDayKey = selectedChartDayKey,
                                onBarTapped = { dayKey ->
                                    if (selectedChartDayKey == dayKey) {
                                        showChartStones = false
                                        selectedChartDayKey = null
                                    } else {
                                        selectedChartDayKey = dayKey
                                        showChartStones = true
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .padding(horizontal = 16.dp)
                            )
                        }
                    }

                    if (selectedChartDayKey != null && selectedChartStones.isNotEmpty() && showChartStones) {
                        item {
                            ChartDayHeader(
                                selectedDayKey = selectedChartDayKey,
                                onClose = {
                                    showChartStones = false
                                    selectedChartDayKey = null
                                }
                            )
                        }
                        itemsIndexed(selectedChartStones, key = { _, stone -> stone.id }) { index, stone ->
                            StoneTimelineItem(
                                stone = stone,
                                isFirst = index == 0,
                                isLast = index == selectedChartStones.lastIndex,
                                totalCount = selectedChartStones.size,
                                onClick = { onNavigateToStoneDetail(stone.id) }
                            )
                        }
                    }
                }

                1 -> item {
                    SectionTitle("All-time")
                    if (monthlyData.isEmpty()) {
                        EmptyStateView(
                            message = "Add some stones to see all-time totals.",
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    } else {
                        MonthlyStackedBarChart(
                            data = monthlyData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(horizontal = 16.dp)
                        )
                    }
                }

                2 -> item {
                    SectionTitle("Patterns")
                    Text(
                        text = "Patterns will appear here once you've logged a few weeks of stones.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                SectionTitle("Calendar")
            }

            item {
                MonthNavigation(
                    displayedMonth = displayedMonth,
                    onPrevious = { displayedMonth = DateHelpers.offsetMonth(displayedMonth, -1) },
                    onNext = { displayedMonth = DateHelpers.offsetMonth(displayedMonth, 1) }
                )
            }

            item { WeekdayHeader() }

            val daysInMonth = DateHelpers.daysInMonth(displayedMonth)
            val weekdayOffset = DateHelpers.weekdayOfFirst(displayedMonth)
            val totalCells = weekdayOffset + daysInMonth
            val rows = (totalCells + 6) / 7
            for (row in 0 until rows) {
                item {
                    CalendarRow(
                        row = row,
                        daysInMonth = daysInMonth,
                        weekdayOffset = weekdayOffset,
                        displayedMonth = displayedMonth,
                        selectedDayKey = selectedDayKey,
                        ratioByDay = ratioByDay,
                        reflectionDayKeys = reflectionDayKeys.toSet(),
                        onDaySelected = { selectedDayKey = it }
                    )
                }
            }

            item {
                SelectedDaySummary(
                    selectedDayKey = selectedDayKey,
                    selectedStones = selectedStones
                )
            }

            if (selectedDayKey != null) {
                if (selectedStones.isEmpty()) {
                    item {
                        if (selectedReflection == null) {
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

                selectedReflection?.let { reflection ->
                    item {
                        SectionTitle("Reflection")
                    }
                    item {
                        DayReflectionCard(
                            reflection = reflection,
                            onClick = { onNavigateToReflectionDetail(reflection.dayKey) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatStrip(
    totalDaysTracked: Int,
    monthWhiteCount: Int,
    monthBlackCount: Int,
    currentStreak: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Review",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Total days tracked: $totalDaysTracked",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "This month: $monthWhiteCount white · $monthBlackCount black",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Streak: ${currentStreak}d",
            style = MaterialTheme.typography.bodyMedium,
            color = BrownAccent,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MonthNavigation(
    displayedMonth: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = DateHelpers.monthYearString(displayedMonth),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onNext) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
        }
    }
}

@Composable
private fun WeekdayHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        listOf("M", "T", "W", "T", "F", "S", "S").forEach { symbol ->
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

@Composable
private fun CalendarRow(
    row: Int,
    daysInMonth: Int,
    weekdayOffset: Int,
    displayedMonth: LocalDate,
    selectedDayKey: String?,
    ratioByDay: Map<String, Double?>,
    reflectionDayKeys: Set<String>,
    onDaySelected: (String) -> Unit
) {
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
                DayCell(
                    day = day,
                    ratio = ratioByDay[key],
                    isSelected = selectedDayKey == key,
                    hasReflection = key in reflectionDayKeys,
                    onClick = { onDaySelected(key) },
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

@Composable
private fun DayCell(
    day: Int,
    ratio: Double?,
    isSelected: Boolean,
    hasReflection: Boolean,
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
        if (hasReflection) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(5.dp)
                    .width(5.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(BrownAccent)
            )
        }
    }
}

@Composable
private fun SelectedDaySummary(
    selectedDayKey: String?,
    selectedStones: List<Stone>
) {
    if (selectedDayKey == null) return

    val selectedWhiteCount = selectedStones.count { it.type == StoneType.WHITE }
    val selectedBlackCount = selectedStones.count { it.type == StoneType.BLACK }

    Spacer(modifier = Modifier.height(8.dp))
    if (selectedStones.isNotEmpty()) {
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
    Text(
        text = "Stones",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun ChartDayHeader(
    selectedDayKey: String?,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val date = selectedDayKey?.let { DateHelpers.dateFromDayKey(it) }
        if (date != null) {
            Text(
                text = DateHelpers.fullDateString(date),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onClose) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StackedBarChart(
    data: List<DayBarData>,
    selectedDayKey: String?,
    onBarTapped: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val whiteBarColor = LightGray
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
                    val whiteTop = chartHeight - totalHeight
                    val blackTop = chartHeight - blackHeight

                    drawRoundRect(
                        color = blackBarColor.copy(alpha = alpha),
                        topLeft = Offset(x, blackTop),
                        size = Size(barWidth, blackHeight),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )

                    drawRoundRect(
                        color = whiteBarColor.copy(alpha = alpha),
                        topLeft = Offset(x, whiteTop),
                        size = Size(barWidth, whiteHeight),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                }

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

@Composable
private fun MonthlyStackedBarChart(
    data: List<MonthBarData>,
    modifier: Modifier = Modifier
) {
    val whiteBarColor = LightGray
    val blackBarColor = Color(0xFF333333)
    val maxTotal = data.maxOfOrNull { it.whiteCount + it.blackCount } ?: 1

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val spacing = 8.dp.toPx()
            val barWidth = (size.width - spacing * (data.size - 1)) / data.size.coerceAtLeast(1)
            val labelHeight = 34.dp.toPx()
            val chartHeight = size.height - labelHeight

            data.forEachIndexed { index, bar ->
                val x = index * (barWidth + spacing)
                val total = bar.whiteCount + bar.blackCount
                if (total > 0) {
                    val totalHeight = (total.toFloat() / maxTotal) * chartHeight
                    val blackHeight = (bar.blackCount.toFloat() / maxTotal) * chartHeight
                    val whiteHeight = totalHeight - blackHeight
                    val whiteTop = chartHeight - totalHeight
                    val blackTop = chartHeight - blackHeight

                    drawRoundRect(
                        color = blackBarColor,
                        topLeft = Offset(x, blackTop),
                        size = Size(barWidth, blackHeight),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                    drawRoundRect(
                        color = whiteBarColor,
                        topLeft = Offset(x, whiteTop),
                        size = Size(barWidth, whiteHeight),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                }

                val paint = android.graphics.Paint().apply {
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                    color = android.graphics.Color.GRAY
                }
                drawContext.canvas.nativeCanvas.drawText(
                    bar.label,
                    x + barWidth / 2,
                    chartHeight + 20.dp.toPx(),
                    paint
                )
            }
        }
    }
}

private fun currentStreak(stones: List<Stone>): Int {
    var streak = 0
    var date = LocalDate.now()
    while (true) {
        val key = DateHelpers.dayKey(date)
        val dayStones = stones.filter { it.dayKey == key }
        if (dayStones.isEmpty()) break
        val white = dayStones.count { it.type == StoneType.WHITE }
        if (white * 2 >= dayStones.size) {
            streak++
        } else {
            break
        }
        date = date.minusDays(1)
    }
    return streak
}

private fun allTimeMonthlyData(stones: List<Stone>): List<MonthBarData> {
    if (stones.isEmpty()) return emptyList()

    val months = stones.mapNotNull { DateHelpers.dateFromDayKey(it.dayKey) }
        .map { YearMonth.from(it) }
        .distinct()
        .sorted()

    return months.map { month ->
        val monthStones = stones.filter {
            val date = DateHelpers.dateFromDayKey(it.dayKey)
            date != null && YearMonth.from(date) == month
        }
        MonthBarData(
            label = month.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
            whiteCount = monthStones.count { it.type == StoneType.WHITE },
            blackCount = monthStones.count { it.type == StoneType.BLACK }
        )
    }
}
