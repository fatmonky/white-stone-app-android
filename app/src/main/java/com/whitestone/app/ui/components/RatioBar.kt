package com.whitestone.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.unit.dp
import com.whitestone.app.util.ColorHelpers

@Composable
fun RatioBar(white: Int, black: Int, modifier: Modifier = Modifier) {
    val total = white + black
    val ratio = if (total > 0) white.toFloat() / total.toFloat() else 0.5f
    val blackColor = ColorHelpers.colorForRatio(0.0)
    val whiteColor = ColorHelpers.colorForRatio(1.0)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .drawBehind { drawRect(blackColor) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(ratio)
                .align(Alignment.CenterStart)
                .drawBehind { drawRect(whiteColor) }
        )
    }
}
