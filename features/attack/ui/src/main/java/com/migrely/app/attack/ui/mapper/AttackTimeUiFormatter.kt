package com.migrely.app.attack.ui.mapper

import java.time.DateTimeException
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Formats the details screen's times in the zone where the attack started, with that zone's city when it differs from
 * the device zone (BRD EC-TIM-02), in the device's locale and 12- or 24-hour setting (BRD EC-TIM-05). Inputs are
 * functions so the current settings are read at every call.
 */
class AttackTimeUiFormatter(
    private val locale: () -> Locale,
    private val is24HourFormat: () -> Boolean,
    private val deviceZone: () -> ZoneId,
    private val bestDateTimePattern: (Locale, String) -> String
) {
    /** "14:05", or "14:05 (Tashkent)" away from the attack's zone. */
    fun formatClock(epochMillis: Long, zoneId: String): String {
        val zone = zoneOf(zoneId)
        return withCity(
            pattern(clockSkeleton()).format(Instant.ofEpochMilli(epochMillis).atZone(zone)),
            zone
        )
    }

    /** "Fri 23 Oct, 14:05", with the city away from the attack's zone. */
    fun formatDateTime(epochMillis: Long, zoneId: String): String {
        val zone = zoneOf(zoneId)
        val time = Instant.ofEpochMilli(epochMillis).atZone(zone)
        return withCity(
            "${pattern("EEEdMMM").format(time)}, ${pattern(clockSkeleton()).format(time)}",
            zone
        )
    }

    /** The clock time when [epochMillis] falls on the same day as [referenceEpochMillis], otherwise the date too. */
    fun formatWithinAttack(epochMillis: Long, referenceEpochMillis: Long, zoneId: String): String {
        val zone = zoneOf(zoneId)
        val sameDay = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate() ==
            Instant.ofEpochMilli(referenceEpochMillis).atZone(zone).toLocalDate()
        return if (sameDay) {
            formatClock(epochMillis, zoneId)
        } else {
            formatDateTime(
                epochMillis,
                zoneId
            )
        }
    }

    /** "Friday, 23 October" for the day the attack started. */
    fun formatDay(epochMillis: Long, zoneId: String): String =
        pattern("EEEEdMMMM").format(Instant.ofEpochMilli(epochMillis).atZone(zoneOf(zoneId)))

    private fun clockSkeleton() = if (is24HourFormat()) "Hm" else "hm"

    private fun pattern(skeleton: String) =
        DateTimeFormatter.ofPattern(bestDateTimePattern(locale(), skeleton), locale())

    private fun withCity(text: String, zone: ZoneId): String =
        if (zone.normalized() == deviceZone().normalized()) text else "$text (${zone.city()})"

    private fun ZoneId.city(): String = when (this) {
        is ZoneOffset -> getDisplayName(TextStyle.SHORT, locale())
        else -> id.substringAfterLast('/').replace('_', ' ')
    }

    // A zone id the device no longer knows falls back to the device zone.
    private fun zoneOf(id: String): ZoneId = try {
        ZoneId.of(id)
    } catch (_: DateTimeException) {
        deviceZone()
    }
}
