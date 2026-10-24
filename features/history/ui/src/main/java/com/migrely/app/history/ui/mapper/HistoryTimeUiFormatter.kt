package com.migrely.app.history.ui.mapper

import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Formats dates and times for History. A time is shown in the zone where it happened, with that zone's city when it
 * differs from the device zone (BRD EC-TIM-02). Inputs are functions so the current locale, 24-hour setting and device
 * zone are read at every call (BRD EC-TIM-05).
 */
class HistoryTimeUiFormatter(
    private val locale: () -> Locale,
    private val is24HourFormat: () -> Boolean,
    private val deviceZone: () -> ZoneId,
    private val bestDateTimePattern: (Locale, String) -> String
) {
    /** "October 2026" for the month header. */
    fun formatMonthTitle(year: Int, month: Int): String =
        format("MMMMy", YearMonth.of(year, month).atDay(1))

    /** "October" for the month card and the list titles. */
    fun formatMonthName(year: Int, month: Int): String =
        format("MMMM", YearMonth.of(year, month).atDay(1))

    /** "Tuesday, 14 October" for a chosen day and for what a screen reader says about a day. */
    fun formatDayTitle(epochDay: Long): String = format("EEEEdMMMM", LocalDate.ofEpochDay(epochDay))

    /**
     * The start of a listed attack: "Tue 14 Oct, 09:10". [spoken] gives the full weekday and month for screen readers.
     */
    fun formatStart(epochMillis: Long, zoneId: String, spoken: Boolean = false): String {
        val zone = zoneOf(zoneId)
        val date = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
        val day = format(if (spoken) "EEEEdMMMM" else "EEEdMMM", date)
        return "$day, ${formatClockTime(epochMillis, zoneId)}"
    }

    /** The end of a listed attack: its clock time, with the weekday when it ended on another day than it started. */
    fun formatEnd(endEpochMillis: Long, startEpochMillis: Long, zoneId: String): String {
        val zone = zoneOf(zoneId)
        val endDate = Instant.ofEpochMilli(endEpochMillis).atZone(zone).toLocalDate()
        val startDate = Instant.ofEpochMilli(startEpochMillis).atZone(zone).toLocalDate()
        val time = formatClockTime(endEpochMillis, zoneId)
        return if (endDate == startDate) time else "${format("EEE", endDate)} $time"
    }

    fun formatClockTime(epochMillis: Long, zoneId: String): String {
        val zone = zoneOf(zoneId)
        val skeleton = if (is24HourFormat()) "Hm" else "hm"
        val time = DateTimeFormatter.ofPattern(bestDateTimePattern(locale(), skeleton), locale())
            .format(Instant.ofEpochMilli(epochMillis).atZone(zone))
        return if (zone.normalized() == deviceZone().normalized()) {
            time
        } else {
            "$time (${zone.id.substringAfterLast('/').replace('_', ' ')})"
        }
    }

    private fun format(skeleton: String, date: LocalDate): String =
        DateTimeFormatter.ofPattern(bestDateTimePattern(locale(), skeleton), locale()).format(date)

    @Suppress("SwallowedException")
    private fun zoneOf(zoneId: String): ZoneId = try {
        ZoneId.of(zoneId)
    } catch (exception: DateTimeException) {
        deviceZone()
    }
}
