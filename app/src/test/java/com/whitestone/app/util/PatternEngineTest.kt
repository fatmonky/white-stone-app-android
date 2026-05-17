package com.whitestone.app.util

import com.whitestone.app.data.Stone
import com.whitestone.app.data.StoneIntensity
import com.whitestone.app.data.StoneRoot
import com.whitestone.app.data.StoneType
import com.whitestone.app.data.toRootTagsCsv
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PatternEngineTest {
    private val zone = ZoneOffset.UTC
    private val now = date(day = 20, hour = 12)

    @Test
    fun returnsNoObservationsWhenThereIsNotEnoughData() {
        val observations = PatternEngine.observe(
            stones = listOf(
                stone(StoneType.WHITE, day = 20, hour = 9),
                stone(StoneType.BLACK, day = 19, hour = 15)
            ),
            now = now,
            zone = zone
        )

        assertTrue(observations.isEmpty())
    }

    @Test
    fun timeOfDayClusteringRequiresDominantBucket() {
        val stones = (0 until 6).map { stone(StoneType.BLACK, day = 20, hour = 15, minute = it) } +
            (0 until 4).map { stone(StoneType.BLACK, day = 19, hour = 9, minute = it) }

        val observations = PatternEngine.observe(stones, now, zone)

        assertEquals(
            "in the last two weeks, your black stones often appeared between 14:00 and 18:00.",
            observations.first().text
        )
    }

    @Test
    fun mostTaggedRootUsesRecentRootTags() {
        val stones = listOf(
            stone(StoneType.BLACK, day = 20, hour = 8, roots = listOf(StoneRoot.ILL_WILL)),
            stone(StoneType.BLACK, day = 20, hour = 9, roots = listOf(StoneRoot.ILL_WILL)),
            stone(StoneType.BLACK, day = 19, hour = 8, roots = listOf(StoneRoot.ILL_WILL)),
            stone(StoneType.BLACK, day = 19, hour = 9, roots = listOf(StoneRoot.HARMING)),
            stone(StoneType.WHITE, day = 18, hour = 8, roots = listOf(StoneRoot.KINDNESS))
        )

        val observations = PatternEngine.observe(stones, now, zone)

        assertTrue(observations.any {
            it.text == "most-tagged root in the last two weeks: ill will."
        })
    }

    @Test
    fun mostTaggedRootBreaksTiesAlphabetically() {
        val stones = listOf(
            stone(StoneType.BLACK, day = 20, hour = 8, roots = listOf(StoneRoot.SENSUAL)),
            stone(StoneType.BLACK, day = 20, hour = 9, roots = listOf(StoneRoot.SENSUAL)),
            stone(StoneType.BLACK, day = 19, hour = 8, roots = listOf(StoneRoot.HARMING)),
            stone(StoneType.BLACK, day = 19, hour = 9, roots = listOf(StoneRoot.HARMING)),
            stone(StoneType.WHITE, day = 18, hour = 8, roots = listOf(StoneRoot.KINDNESS))
        )

        val observations = PatternEngine.observe(stones, now, zone)

        assertTrue(observations.any {
            it.text == "most-tagged root in the last two weeks: harming."
        })
    }

    @Test
    fun intensityTiltRendersOnlyTwoToOneTilts() {
        val stones = listOf(
            stone(StoneType.WHITE, day = 20, hour = 8, intensity = StoneIntensity.STRONG),
            stone(StoneType.WHITE, day = 20, hour = 9, intensity = StoneIntensity.STRONG),
            stone(StoneType.BLACK, day = 19, hour = 8, intensity = StoneIntensity.STRONG),
            stone(StoneType.BLACK, day = 19, hour = 9, intensity = StoneIntensity.STRONG),
            stone(StoneType.WHITE, day = 18, hour = 8, intensity = StoneIntensity.WEAK)
        )

        val observations = PatternEngine.observe(stones, now, zone)

        assertTrue(observations.any {
            it.text == "in the last two weeks, your stones have leaned strong."
        })
    }

    @Test
    fun intensityColorCrossTagRendersDominantColorForIntensity() {
        val stones = listOf(
            stone(StoneType.BLACK, day = 20, hour = 8, intensity = StoneIntensity.STRONG),
            stone(StoneType.BLACK, day = 20, hour = 9, intensity = StoneIntensity.STRONG),
            stone(StoneType.BLACK, day = 19, hour = 8, intensity = StoneIntensity.STRONG),
            stone(StoneType.BLACK, day = 19, hour = 9, intensity = StoneIntensity.STRONG),
            stone(StoneType.WHITE, day = 18, hour = 8, intensity = StoneIntensity.STRONG)
        )

        val observations = PatternEngine.observe(stones, now, zone)

        assertTrue(observations.any {
            it.text == "your strong stones recently have mostly been black."
        })
    }

    @Test
    fun loggingCadenceObservesMostDaysInLastTwoWeeks() {
        val stones = (11..20).map { day ->
            stone(StoneType.WHITE, day = day, hour = 8)
        }

        val observations = PatternEngine.observe(stones, now, zone)

        assertTrue(observations.any {
            it.text == "you've been logging on most days in the last two weeks."
        })
    }

    @Test
    fun loggingCadenceObservesFewDaysSinceLastEntry() {
        val stones = listOf(
            stone(StoneType.WHITE, day = 15, hour = 8),
            stone(StoneType.BLACK, day = 14, hour = 8)
        )

        val observations = PatternEngine.observe(stones, now, zone)

        assertEquals(
            listOf("it's been a few days since your last entry."),
            observations.map { it.text }
        )
    }

    @Test
    fun limitsRenderedObservationsToFour() {
        val stones = (0 until 12).map {
            stone(
                StoneType.BLACK,
                day = 20,
                hour = 15,
                minute = it,
                roots = listOf(StoneRoot.ILL_WILL),
                intensity = StoneIntensity.STRONG
            )
        } +
            (0 until 3).map {
                stone(
                    StoneType.WHITE,
                    day = 19,
                    hour = 8,
                    minute = it,
                    roots = listOf(StoneRoot.KINDNESS),
                    intensity = StoneIntensity.STRONG
                )
            } +
            listOf(
                stone(StoneType.WHITE, day = 18, hour = 8),
                stone(StoneType.WHITE, day = 17, hour = 8),
                stone(StoneType.WHITE, day = 16, hour = 8)
            )

        val observations = PatternEngine.observe(stones, now, zone)

        assertEquals(4, observations.size)
    }

    private fun stone(
        type: StoneType,
        day: Int,
        hour: Int,
        minute: Int = 0,
        roots: List<StoneRoot> = emptyList(),
        intensity: StoneIntensity? = null
    ): Stone {
        val timestamp = date(day = day, hour = hour, minute = minute)
            .toEpochMilli()
        return Stone(
            type = type,
            timestamp = timestamp,
            dayKey = DateHelpers.dayKey(
                LocalDateTime.of(2026, 5, day, hour, minute)
                    .toLocalDate()
            ),
            rootTagsCsv = roots.toRootTagsCsv(),
            intensity = intensity?.rawValue
        )
    }

    private fun date(day: Int, hour: Int, minute: Int = 0): Instant {
        return LocalDateTime.of(2026, 5, day, hour, minute)
            .toInstant(zone)
    }
}
