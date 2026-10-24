package com.migrely.app.medication.ui.mapper

import java.time.DateTimeException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Formats intake times in the zone where they were taken (BRD EC-TIM-02): the clock time today, with the short date
 * before it on another day. Inputs are functions so the locale, 24-hour setting and device zone are read at every call.
 */
class MedicationTimeUiFormatter(
    private val locale: () -> Locale,
    private val is24HourFormat: () -> Boolean,
    private val deviceZone: () -> ZoneId,
    private val bestDateTimePattern: (Locale, String) -> String
) {
    fun formatTime(epochMillis: Long, zoneId: String, nowEpochMillis: Long): String {
        val zone = zoneOf(zoneId)
        val at = Instant.ofEpochMilli(epochMillis).atZone(zone)
        val today = Instant.ofEpochMilli(nowEpochMillis).atZone(zone).toLocalDate()
        val time = DateTimeFormatter.ofPattern(
            bestDateTimePattern(locale(), if (is24HourFormat()) "Hm" else "hm"),
            locale()
        ).format(at)
        if (at.toLocalDate() == today) return time
        val day = DateTimeFormatter.ofPattern(bestDateTimePattern(locale(), "EEEdMMM"), locale())
            .format(at)
        return "$day, $time"
    }

    @Suppress("SwallowedException")
    private fun zoneOf(zoneId: String): ZoneId = try {
        ZoneId.of(zoneId)
    } catch (exception: DateTimeException) {
        deviceZone()
    }
}
