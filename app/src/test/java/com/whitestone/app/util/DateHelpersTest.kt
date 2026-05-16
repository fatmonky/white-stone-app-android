package com.whitestone.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.util.TimeZone

class DateHelpersTest {

    @Test
    fun dayKeyUsesSystemTimeZoneForTimestamp() {
        val previous = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        try {
            val timestamp = LocalDate.of(2026, 2, 10)
                .atStartOfDay(ZoneId.of("UTC"))
                .toInstant()
                .toEpochMilli()
            assertEquals("2026-02-10", DateHelpers.dayKey(timestamp))
        } finally {
            TimeZone.setDefault(previous)
        }
    }

    @Test
    fun dateFromDayKeyReturnsNullForInvalidInput() {
        assertNull(DateHelpers.dateFromDayKey("not-a-date"))
        assertEquals(LocalDate.of(2026, 2, 10), DateHelpers.dateFromDayKey("2026-02-10"))
    }

    @Test
    fun weekdayOfFirstUsesMondayAsZero() {
        assertEquals(6, DateHelpers.weekdayOfFirst(LocalDate.of(2026, 2, 10)))
        assertEquals(0, DateHelpers.weekdayOfFirst(LocalDate.of(2025, 9, 3)))
    }

    @Test
    fun startOfWeekReturnsSunday() {
        assertEquals(
            LocalDate.of(2026, 3, 8),
            DateHelpers.startOfWeek(LocalDate.of(2026, 3, 8))
        )
        assertEquals(
            LocalDate.of(2026, 3, 8),
            DateHelpers.startOfWeek(LocalDate.of(2026, 3, 10))
        )
    }

    @Test
    fun dayKeysForPastWeeksReturnsContiguousSundayToSaturdayWeeks() {
        val weeks = DateHelpers.dayKeysForPastWeeks(3)

        assertEquals(3, weeks.size)
        weeks.forEach { week -> assertEquals(7, week.size) }
        assertNotNull(DateHelpers.dateFromDayKey(weeks.first().first()))
        assertTrue(weeks.zipWithNext().all { (current, next) -> current.last() < next.first() })
        weeks.forEach { week ->
            val start = DateHelpers.dateFromDayKey(week.first())!!
            val end = DateHelpers.dateFromDayKey(week.last())!!
            assertEquals(start.plusDays(6), end)
        }
    }
}
