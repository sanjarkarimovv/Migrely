package com.migrely.app.attack.ui.mapper

import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

// 14:05 on Friday 23 October 2026 in Tashkent (UTC+5).
private val START = Instant.parse("2026-10-23T09:05:00Z").toEpochMilli()
private const val DAY = 24 * 3_600_000L

class AttackTimeUiFormatterTest {
    private fun formatter(deviceZone: String = "Asia/Tashkent", is24Hour: Boolean = true) =
        AttackTimeUiFormatter(
            locale = { Locale.UK },
            is24HourFormat = { is24Hour },
            deviceZone = { ZoneId.of(deviceZone) },
            bestDateTimePattern = { _, skeleton ->
                when (skeleton) {
                    "Hm" -> "HH:mm"
                    "hm" -> "h:mm a"
                    "EEEdMMM" -> "EEE d MMM"
                    else -> "EEEE, d MMMM"
                }
            }
        )

    @Test
    fun `When formatting a date and time then weekday, date and clock`() {
        assertEquals("Fri 23 Oct, 14:05", formatter().formatDateTime(START, "Asia/Tashkent"))
    }

    @Test
    fun `Given the user is in Berlin then shows Tashkent time with its city`() {
        assertEquals(
            "14:05 (Tashkent)",
            formatter(deviceZone = "Europe/Berlin").formatClock(START, "Asia/Tashkent")
        )
    }

    @Test
    fun `Given 12-hour format then uses am and pm`() {
        assertEquals(
            "2:05 pm",
            formatter(is24Hour = false).formatClock(START, "Asia/Tashkent").lowercase(Locale.UK)
        )
    }

    @Test
    fun `Given a time on the attack's first day then only the clock`() {
        assertEquals(
            "15:05",
            formatter().formatWithinAttack(START + 3_600_000L, START, "Asia/Tashkent")
        )
    }

    @Test
    fun `Given a time on a later day then the date too`() {
        assertEquals(
            "Sat 24 Oct, 14:05",
            formatter().formatWithinAttack(START + DAY, START, "Asia/Tashkent")
        )
    }

    @Test
    fun `When formatting the day then the full weekday and date`() {
        assertEquals("Friday, 23 October", formatter().formatDay(START, "Asia/Tashkent"))
    }

    @Test
    fun `Given an unknown zone then falls back to the device zone`() {
        assertEquals("14:05", formatter().formatClock(START, "Mars/Base"))
    }
}
