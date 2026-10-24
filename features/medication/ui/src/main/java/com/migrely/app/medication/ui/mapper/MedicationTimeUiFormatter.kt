package com.migrely.app.medication.ui.mapper

import java.time.DateTimeException
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Formats intake times in the zone where they were taken (BRD EC-TIM-02): the clock time today, with the short date
 * before it on another day, and that zone's city when it differs from the device's. Inputs are functions so the
 * locale, 24-hour setting and device zone are read at every call.
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
        val text = if (at.toLocalDate() == today) {
            time
        } else {
            val day = DateTimeFormatter.ofPattern(
                bestDateTimePattern(locale(), "EEEdMMM"),
                locale()
            )
                .format(at)
            "$day, $time"
        }
        return if (zone.normalized() ==
            deviceZone().normalized()
        ) {
            text
        } else {
            "$text (${zone.city()})"
        }
    }

    private fun ZoneId.city(): String = when (this) {
        is ZoneOffset -> getDisplayName(TextStyle.SHORT, locale())
        else -> id.substringAfterLast('/').replace('_', ' ')
    }

    @Suppress("SwallowedException")
    private fun zoneOf(zoneId: String): ZoneId = try {
        ZoneId.of(zoneId)
    } catch (exception: DateTimeException) {
        deviceZone()
    }
}
