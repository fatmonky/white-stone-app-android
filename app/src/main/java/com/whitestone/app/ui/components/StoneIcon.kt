package com.whitestone.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.whitestone.app.data.StoneType

@Composable
fun StoneIcon(type: StoneType, size: Dp, modifier: Modifier = Modifier) {
    if (size >= 60.dp) {
        LargeStone(type = type, size = size, modifier = modifier)
    } else {
        SmallStone(type = type, size = size, modifier = modifier)
    }
}

@Composable
private fun SmallStone(type: StoneType, size: Dp, modifier: Modifier = Modifier) {
    val fillColor = if (type == StoneType.WHITE) Color.White else Color.Black
    Box(
        modifier = modifier
            .size(size)
            .border(1.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            drawCircle(color = fillColor)
        }
    }
}

@Composable
private fun LargeStone(type: StoneType, size: Dp, modifier: Modifier = Modifier) {
    val baseColor = if (type == StoneType.WHITE) Color.White else Color.Black
    val highlightColor = if (type == StoneType.WHITE) Color.White else Color(0xFF404040)
    val shadowColor = if (type == StoneType.WHITE) Color(0xFFBFBFBF) else Color.Black
    val specularColor = if (type == StoneType.WHITE) Color.White.copy(alpha = 0.6f) else Color(0xFF666666).copy(alpha = 0.6f)
    val dropShadowAlpha = if (type == StoneType.WHITE) 0.2f else 0.5f

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = (size.value * 0.02f).dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = dropShadowAlpha),
                spotColor = Color.Black.copy(alpha = dropShadowAlpha)
            )
    ) {
        Canvas(modifier = Modifier.size(size)) {
            drawLargeStone(
                baseColor = baseColor,
                highlightColor = highlightColor,
                shadowColor = shadowColor,
                specularColor = specularColor
            )
        }
    }
}

private fun DrawScope.drawLargeStone(
    baseColor: Color,
    highlightColor: Color,
    shadowColor: Color,
    specularColor: Color
) {
    val radius = size.minDimension / 2f

    // Base gradient for 3D curvature — light from top-left
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(highlightColor, baseColor, shadowColor),
            center = Offset(size.width * 0.35f, size.height * 0.3f),
            radius = radius * 1.2f
        )
    )

    // Subtle specular highlight
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(specularColor, Color.Transparent),
            center = Offset(size.width * 0.3f, size.height * 0.25f),
            radius = radius * 0.5f
        )
    )

    // Fine surface texture using sweep gradient
    drawCircle(
        brush = Brush.sweepGradient(
            colors = listOf(
                baseColor.copy(alpha = 0.0f),
                baseColor.copy(alpha = 0.04f),
                baseColor.copy(alpha = 0.0f),
                baseColor.copy(alpha = 0.03f),
                baseColor.copy(alpha = 0.0f),
                baseColor.copy(alpha = 0.05f),
                baseColor.copy(alpha = 0.0f),
            ),
            center = Offset(size.width * 0.45f, size.height * 0.45f)
        )
    )

    // Edge definition
    drawCircle(
        color = Color(0xFF808080),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.5f)
    )
}
