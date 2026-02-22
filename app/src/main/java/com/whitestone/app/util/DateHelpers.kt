package com.whitestone.app.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object DateHelpers {

    private val dayKeyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    fun dayKey(timestampMillis: Long): String {
        val date = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestampMillis),
            ZoneId.systemDefault()
        ).toLocalDate()
        return date.format(dayKeyFormatter)
    }

    fun dayKey(date: LocalDate): String = date.format(dayKeyFormatter)

    val todayKey: String get() = dayKey(LocalDate.now())

    fun dateFromDayKey(dayKey: String): LocalDate? {
        return try {
            LocalDate.parse(dayKey, dayKeyFormatter)
        } catch (_: Exception) {
            null
        }
    }

    fun firstOfMonth(date: LocalDate): LocalDate =
        date.withDayOfMonth(1)

    fun daysInMonth(date: LocalDate): Int =
        date.lengthOfMonth()

    /** Weekday index (0 = Monday) of the first day of the month containing date. */
    fun weekdayOfFirst(date: LocalDate): Int {
        val first = firstOfMonth(date)
        return first.dayOfWeek.value - 1 // Monday=0, ..., Sunday=6
    }

    fun offsetMonth(date: LocalDate, months: Int): LocalDate =
        date.plusMonths(months.toLong())

    /** Display string like "February 2026". */
    fun monthYearString(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        return date.format(formatter)
    }

    /** Short time string like "2:34 PM" from epoch millis. */
    fun timeString(timestampMillis: Long): String {
        val dt = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestampMillis),
            ZoneId.systemDefault()
        )
        val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
        return dt.format(formatter)
    }

    /** Full date string like "Monday, February 10, 2026". */
    fun fullDateString(date: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())
        return date.format(formatter)
    }

    fun fullDateString(timestampMillis: Long): String {
        val date = LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(timestampMillis),
            ZoneId.systemDefault()
        ).toLocalDate()
        return fullDateString(date)
    }

    /** Build a dayKey string for a specific day number within the month of date. */
    fun dayKeyForDay(day: Int, monthDate: LocalDate): String {
        val date = monthDate.withDayOfMonth(day)
        return dayKey(date)
    }

    /** Abbreviated day-of-week for chart axis (M, T, W, Th, F, Sa, Su). */
    fun dayAbbreviation(date: LocalDate): String {
        return when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "M"
            DayOfWeek.TUESDAY -> "T"
            DayOfWeek.WEDNESDAY -> "W"
            DayOfWeek.THURSDAY -> "Th"
            DayOfWeek.FRIDAY -> "F"
            DayOfWeek.SATURDAY -> "Sa"
            DayOfWeek.SUNDAY -> "Su"
        }
    }

    /** Day number as string (e.g. "11"). */
    fun dayNumber(date: LocalDate): String = "${date.dayOfMonth}"

    /** Start of the week containing date (Sunday). */
    fun startOfWeek(date: LocalDate): LocalDate =
        date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))

    /** All dayKeys for the past `weeks` weeks ending at the current week. */
    fun dayKeysForPastWeeks(weeks: Int): List<List<String>> {
        val today = LocalDate.now()
        val result = mutableListOf<List<String>>()
        for (weekOffset in -(weeks - 1)..0) {
            val weekStart = startOfWeek(today).plusWeeks(weekOffset.toLong())
            val week = (0 until 7).map { dayOffset ->
                dayKey(weekStart.plusDays(dayOffset.toLong()))
            }
            result.add(week)
        }
        return result
    }
}
