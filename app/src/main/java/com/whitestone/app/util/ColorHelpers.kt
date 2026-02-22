package com.whitestone.app.util

import androidx.compose.ui.graphics.Color

object ColorHelpers {

    /** Maps a white-stone ratio (0.0 = all black, 1.0 = all white) to a colour. */
    fun colorForRatio(ratio: Double?): Color {
        if (ratio == null) return Color.Gray.copy(alpha = 0.15f)
        val brightness = (0.15 + 0.75 * ratio).toFloat()
        return Color(brightness, brightness, brightness)
    }

    val whiteStoneColor: Color = Color(0.82f, 0.82f, 0.82f)
    val blackStoneColor: Color = Color(0.2f, 0.2f, 0.2f)

    /** Ratio of white stones to total stones. Returns null if total is 0. */
    fun ratio(white: Int, total: Int): Double? {
        if (total <= 0) return null
        return white.toDouble() / total.toDouble()
    }
}
