package com.whitestone.app.util

import com.whitestone.app.data.Stone
import com.whitestone.app.data.StoneIntensity
import com.whitestone.app.data.StoneType
import com.whitestone.app.data.rootDisplayNames
import com.whitestone.app.data.stoneIntensity
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class Observation(
    val text: String,
    val priority: Int
)

object PatternEngine {
    fun observe(
        stones: List<Stone>,
        now: Instant,
        zone: ZoneId = ZoneId.systemDefault()
    ): List<Observation> {
        return listOfNotNull(
            timeOfDayClustering(stones, now, zone),
            mostTaggedRoot(stones, now, zone),
            intensityTilt(stones, now, zone),
            intensityColorCrossTag(stones, now, zone),
            loggingCadence(stones, now, zone)
        ).take(4)
    }

    private fun timeOfDayClustering(
        stones: List<Stone>,
        now: Instant,
        zone: ZoneId
    ): Observation? {
        val recentStones = stonesWithinLastDays(14, stones, now, zone)
        for (type in listOf(StoneType.BLACK, StoneType.WHITE)) {
            val matchingStones = recentStones.filter { it.type == type }
            if (matchingStones.size < 10) continue

            val dominant = matchingStones
                .groupBy { TimeBucket.from(it.timestamp, zone) }
                .maxByOrNull { it.value.size }

            if (dominant != null && dominant.value.size * 2 > matchingStones.size) {
                return Observation(
                    text = "in the last two weeks, your ${type.displayName} stones often appeared between ${dominant.key.label}.",
                    priority = 1
                )
            }
        }
        return null
    }

    private fun mostTaggedRoot(
        stones: List<Stone>,
        now: Instant,
        zone: ZoneId
    ): Observation? {
        val roots = stonesWithinLastDays(14, stones, now, zone)
            .flatMap { it.rootDisplayNames }
        if (roots.size < 5) return null

        val root = roots
            .groupingBy { it }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .firstOrNull()
            ?.key ?: return null

        return Observation(
            text = "most-tagged root in the last two weeks: $root.",
            priority = 2
        )
    }

    private fun intensityTilt(
        stones: List<Stone>,
        now: Instant,
        zone: ZoneId
    ): Observation? {
        val intensities = stonesWithinLastDays(14, stones, now, zone)
            .mapNotNull { it.stoneIntensity }
        if (intensities.size < 5) return null

        val strong = intensities.count { it == StoneIntensity.STRONG }
        val weak = intensities.count { it == StoneIntensity.WEAK }
        return when {
            strong >= weak * 2 && strong > 0 -> Observation(
                text = "in the last two weeks, your stones have leaned strong.",
                priority = 3
            )
            weak >= strong * 2 && weak > 0 -> Observation(
                text = "in the last two weeks, your stones have leaned weak.",
                priority = 3
            )
            else -> null
        }
    }

    private fun intensityColorCrossTag(
        stones: List<Stone>,
        now: Instant,
        zone: ZoneId
    ): Observation? {
        val taggedStones = stonesWithinLastDays(14, stones, now, zone)
            .filter { it.stoneIntensity != null }
        for (intensity in listOf(StoneIntensity.STRONG, StoneIntensity.WEAK)) {
            val matching = taggedStones.filter { it.stoneIntensity == intensity }
            if (matching.size < 5) continue

            val white = matching.count { it.type == StoneType.WHITE }
            val black = matching.size - white
            if (black * 10 >= matching.size * 7) {
                return Observation(
                    text = "your ${intensity.displayName} stones recently have mostly been black.",
                    priority = 4
                )
            }
            if (white * 10 >= matching.size * 7) {
                return Observation(
                    text = "your ${intensity.displayName} stones recently have mostly been white.",
                    priority = 4
                )
            }
        }
        return null
    }

    private fun loggingCadence(
        stones: List<Stone>,
        now: Instant,
        zone: ZoneId
    ): Observation? {
        if (stones.isEmpty()) return null

        val recentDayKeys = stonesWithinLastDays(14, stones, now, zone)
            .map { Instant.ofEpochMilli(it.timestamp).atZone(zone).toLocalDate() }
            .toSet()
        if (recentDayKeys.size >= 10) {
            return Observation(
                text = "you've been logging on most days in the last two weeks.",
                priority = 5
            )
        }

        val latest = stones.maxOfOrNull { it.timestamp } ?: return null
        val today = now.atZone(zone).toLocalDate()
        val latestDay = Instant.ofEpochMilli(latest).atZone(zone).toLocalDate()
        val daysSinceLatest = ChronoUnit.DAYS.between(latestDay, today)
        if (daysSinceLatest >= 3) {
            return Observation(
                text = "it's been a few days since your last entry.",
                priority = 5
            )
        }
        return null
    }

    private fun stonesWithinLastDays(
        days: Long,
        stones: List<Stone>,
        now: Instant,
        zone: ZoneId
    ): List<Stone> {
        val todayStart = now.atZone(zone).toLocalDate().atStartOfDay(zone).toInstant()
        val start = todayStart.minus(days - 1, ChronoUnit.DAYS)
        val end = todayStart.plus(1, ChronoUnit.DAYS)
        return stones.filter { stone ->
            val timestamp = Instant.ofEpochMilli(stone.timestamp)
            !timestamp.isBefore(start) && timestamp.isBefore(end)
        }
    }
}

private val StoneType.displayName: String
    get() = when (this) {
        StoneType.WHITE -> "white"
        StoneType.BLACK -> "black"
    }

private enum class TimeBucket(val label: String) {
    MORNING("6:00 and 10:00"),
    MIDDAY("10:00 and 14:00"),
    AFTERNOON("14:00 and 18:00"),
    EVENING("18:00 and 22:00"),
    NIGHT("22:00 and 6:00");

    companion object {
        fun from(timestampMillis: Long, zone: ZoneId): TimeBucket {
            return when (Instant.ofEpochMilli(timestampMillis).atZone(zone).hour) {
                in 6 until 10 -> MORNING
                in 10 until 14 -> MIDDAY
                in 14 until 18 -> AFTERNOON
                in 18 until 22 -> EVENING
                else -> NIGHT
            }
        }
    }
}
