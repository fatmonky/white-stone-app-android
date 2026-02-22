package com.whitestone.app.ui.today

import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.whitestone.app.data.StoneType
import com.whitestone.app.ui.addstone.AddStoneSheet
import com.whitestone.app.ui.components.RatioBar
import com.whitestone.app.ui.components.StoneIcon
import com.whitestone.app.ui.components.StoneTimelineItem
import com.whitestone.app.util.DateHelpers
import kotlinx.coroutines.delay
import java.time.LocalDate
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TodayScreen(
    onNavigateToStoneDetail: (Long) -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val allStones by viewModel.allStones.collectAsState(initial = emptyList())
    var currentDate by remember { mutableStateOf(LocalDate.now()) }
    var displayedStoneType by remember { mutableStateOf(StoneType.WHITE) }
    var flipAngle by remember { mutableFloatStateOf(0f) }
    var verticalFlipAngle by remember { mutableFloatStateOf(0f) }
    var holdScale by remember { mutableFloatStateOf(1f) }
    var addStoneType by remember { mutableStateOf<StoneType?>(null) }

    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Vibrator::class.java) }

    val todayKey = DateHelpers.dayKey(currentDate)
    val todayStones = remember(allStones, todayKey) {
        allStones.filter { it.dayKey == todayKey }.sortedBy { it.timestamp }
    }
    val whiteCount = todayStones.count { it.type == StoneType.WHITE }
    val blackCount = todayStones.count { it.type == StoneType.BLACK }

    // Animated values
    val animatedFlipAngle by animateFloatAsState(
        targetValue = flipAngle,
        animationSpec = tween(400),
        label = "flipAngle"
    )
    val animatedVerticalAngle by animateFloatAsState(
        targetValue = verticalFlipAngle,
        animationSpec = tween(600),
        label = "verticalFlipAngle"
    )
    val animatedScale by animateFloatAsState(
        targetValue = holdScale,
        animationSpec = tween(if (holdScale > 1f) 600 else 150),
        label = "holdScale"
    )

    // Pulsing arrows
    val infiniteTransition = rememberInfiniteTransition(label = "arrowPulse")
    val arrowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowAlpha"
    )

    // Midnight rollover detection
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            val now = LocalDate.now()
            if (now != currentDate) {
                currentDate = now
            }
        }
    }

    fun playFlipHaptic() {
        vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        // Second tap after short delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
        }, 120)
    }

    fun flipStone(direction: Float) {
        playFlipHaptic()
        flipAngle += if (direction >= 0) 180f else -180f
        // Swap displayed type at midpoint
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            displayedStoneType = if (displayedStoneType == StoneType.WHITE) StoneType.BLACK else StoneType.WHITE
        }, 200)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "White Stone",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header: date + ratio bar
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp)
                ) {
                    Text(
                        text = DateHelpers.fullDateString(currentDate),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (whiteCount + blackCount > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Your ratio today of good thoughts to bad thoughts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        RatioBar(
                            white = whiteCount,
                            black = blackCount,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }

            // Counts display
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StoneIcon(type = StoneType.WHITE, size = 20.dp)
                        Text("$whiteCount", fontSize = 20.sp, fontWeight = FontWeight.Light)
                    }
                    Spacer(modifier = Modifier.width(32.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StoneIcon(type = StoneType.BLACK, size = 20.dp)
                        Text("$blackCount", fontSize = 20.sp, fontWeight = FontWeight.Light)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            // Flippable stone with arrows
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Swipe left",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = arrowAlpha),
                            modifier = Modifier.width(32.dp)
                        )

                        // The 3D flippable stone
                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    rotationY = animatedFlipAngle
                                    rotationX = animatedVerticalAngle
                                    scaleX = animatedScale
                                    scaleY = animatedScale
                                    cameraDistance = 12f * density
                                }
                                .pointerInput(Unit) {
                                    detectDragGestures { _, dragAmount ->
                                        if (abs(dragAmount.x) > 30f) {
                                            flipStone(dragAmount.x)
                                        } else if (abs(dragAmount.y) > 30f) {
                                            playFlipHaptic()
                                            verticalFlipAngle += if (dragAmount.y >= 0) 360f else -360f
                                        }
                                    }
                                }
                                .combinedClickable(
                                    onClick = { },
                                    onLongClick = {
                                        vibrator?.vibrate(
                                            VibrationEffect.createOneShot(
                                                80,
                                                VibrationEffect.DEFAULT_AMPLITUDE
                                            )
                                        )
                                        holdScale = 1.0f
                                        addStoneType = displayedStoneType
                                    }
                                )
                        ) {
                            StoneIcon(type = displayedStoneType, size = 240.dp)
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Swipe right",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = arrowAlpha),
                            modifier = Modifier.width(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Swipe left/right to flip stone \u2022 Hold to log",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }

            // Today's stones timeline
            if (todayStones.isNotEmpty()) {
                item {
                    Text(
                        text = "Today's Stones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                itemsIndexed(todayStones, key = { _, stone -> stone.id }) { index, stone ->
                    StoneTimelineItem(
                        stone = stone,
                        isFirst = index == 0,
                        isLast = index == todayStones.lastIndex,
                        totalCount = todayStones.size,
                        onClick = { onNavigateToStoneDetail(stone.id) }
                    )
                }
            }
        }
    }

    // Add stone bottom sheet
    addStoneType?.let { type ->
        AddStoneSheet(
            stoneType = type,
            onDismiss = { addStoneType = null },
            onSave = { stoneType, timestamp, note ->
                viewModel.insertStone(stoneType, timestamp, note)
                addStoneType = null
            }
        )
    }
}
