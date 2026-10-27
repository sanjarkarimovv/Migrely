package com.migrely.app.home.ui.mapper

import com.migrely.app.home.ui.model.AttackTimeRangeUiModel
import java.time.DateTimeException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Formats times for the home screen. A time is shown in the zone where it happened, with that zone's city when it
 * differs from the device zone (BRD EC-TIM-02). Inputs are functions so the current locale, 24-hour setting and
 * device zone are read at every call.
 */
class TimeUiFormatter(
    private val locale: () -> Locale,
    private val is24HourFormat: () -> Boolean,
    private val deviceZone: () -> ZoneId,
    private val bestDateTimePattern: (Locale, String) -> String
) {
    fun formatClockTime(epochMillis: Long, zoneId: String): String =
        clockTime(epochMillis, zoneId, withCity = true)

    /**
     * The start of an attack: the clock time when it started today, prefixed with the weekday within the last week
     * ("Tue 14:42") and with the date before that ("12 Oct 14:42"), so a long attack is never ambiguous. Today is the
     * device's date (BRD D36).
     */
    fun formatStartTime(epochMillis: Long, zoneId: String, nowEpochMillis: Long): String =
        startTime(epochMillis, zoneId, nowEpochMillis, withCity = true)

    /**
     * A listed attack's start and end. A city, when shown, comes once, after the end (BRD D36):
     * "23:15 – 23:45 (Berlin)".
     */
    fun formatRange(
        startEpochMillis: Long,
        endEpochMillis: Long,
        zoneId: String,
        nowEpochMillis: Long
    ) = AttackTimeRangeUiModel(
        start = startTime(startEpochMillis, zoneId, nowEpochMillis, withCity = false),
        end = clockTime(endEpochMillis, zoneId, withCity = true)
    )

    fun formatDayTitle(epochMillis: Long): String =
        DateTimeFormatter.ofPattern(bestDateTimePattern(locale(), "EEEEdMMMM"), locale())
            .format(Instant.ofEpochMilli(epochMillis).atZone(deviceZone()))

    private fun clockTime(epochMillis: Long, zoneId: String, withCity: Boolean): String {
        val zone = zoneOf(zoneId)
        val skeleton = if (is24HourFormat()) "Hm" else "hm"
        val time = DateTimeFormatter.ofPattern(bestDateTimePattern(locale(), skeleton), locale())
            .format(Instant.ofEpochMilli(epochMillis).atZone(zone))
        return if (!withCity || zone.normalized() == deviceZone().normalized()) {
            time
        } else {
            "$time (${zone.city()})"
        }
    }

    private fun startTime(
        epochMillis: Long,
        zoneId: String,
        nowEpochMillis: Long,
        withCity: Boolean
    ): String {
        val startDate = Instant.ofEpochMilli(epochMillis).atZone(zoneOf(zoneId)).toLocalDate()
        val today = Instant.ofEpochMilli(nowEpochMillis).atZone(deviceZone()).toLocalDate()
        val time = clockTime(epochMillis, zoneId, withCity)
        if (startDate == today) return time
        val daySkeleton = if (startDate.isAfter(today.minusDays(DAYS_SHOWN_AS_WEEKDAY))) {
            "EEE"
        } else {
            "dMMM"
        }
        val day = DateTimeFormatter.ofPattern(
            bestDateTimePattern(locale(), daySkeleton),
            locale()
        ).format(startDate)
        return "$day $time"
    }

    @Suppress("SwallowedException")
    private fun zoneOf(zoneId: String): ZoneId = try {
        ZoneId.of(zoneId)
    } catch (exception: DateTimeException) {
        deviceZone()
    }

    private fun ZoneId.city(): String = id.substringAfterLast('/').replace('_', ' ')

    private companion object {
        const val DAYS_SHOWN_AS_WEEKDAY = 7L
    }
}
