package com.migrely.app.home.ui.mapper

import com.migrely.app.home.ui.model.AttackTimeRangeUiModel
import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

// 14:05 in Tashkent (UTC+5).
private val START_MILLIS = Instant.parse("2026-10-23T09:05:00Z").toEpochMilli()

class TimeUiFormatterTest {
    private fun formatter(is24Hour: Boolean, deviceZone: String) = TimeUiFormatter(
        locale = { Locale.UK },
        is24HourFormat = { is24Hour },
        deviceZone = { ZoneId.of(deviceZone) },
        bestDateTimePattern = { _, skeleton ->
            when (skeleton) {
                "Hm" -> "HH:mm"
                "hm" -> "h:mm a"
                "EEE" -> "EEE"
                "dMMM" -> "d MMM"
                else -> "EEEE, d MMMM"
            }
        }
    )

    @Test
    fun `Given the attack zone is the device zone when 24-hour then plain time`() {
        val actual = formatter(
            is24Hour = true,
            deviceZone = "Asia/Tashkent"
        ).formatClockTime(START_MILLIS, "Asia/Tashkent")

        assertEquals("14:05", actual)
    }

    @Test
    fun `Given 12-hour format when formatting then uses am-pm`() {
        val actual = formatter(
            is24Hour = false,
            deviceZone = "Asia/Tashkent"
        ).formatClockTime(START_MILLIS, "Asia/Tashkent")

        assertEquals("2:05 pm", actual.lowercase(Locale.UK))
    }

    @Test
    fun `Given the user is in Berlin then shows Tashkent time with its city`() {
        val actual = formatter(
            is24Hour = true,
            deviceZone = "Europe/Berlin"
        ).formatClockTime(START_MILLIS, "Asia/Tashkent")

        assertEquals("14:05 (Tashkent)", actual)
    }

    @Test
    fun `Given an unknown zone id when formatting then falls back to the device zone`() {
        val actual = formatter(
            is24Hour = true,
            deviceZone = "Asia/Tashkent"
        ).formatClockTime(START_MILLIS, "Mars/Base")

        assertEquals("14:05", actual)
    }

    @Test
    fun `When formatting the day title then uses the device zone date`() {
        val actual = formatter(
            is24Hour = true,
            deviceZone = "Asia/Tashkent"
        ).formatDayTitle(START_MILLIS)

        assertEquals("Friday, 23 October", actual)
    }

    @Test
    fun `Given a start today then only the time`() {
        val actual = formatter(true, "Asia/Tashkent").formatStartTime(
            START_MILLIS,
            "Asia/Tashkent",
            START_MILLIS + HOUR
        )

        assertEquals("14:05", actual)
    }

    @Test
    fun `Given a start three days ago then weekday and time`() {
        val now = START_MILLIS + 3 * DAY

        val actual = formatter(
            true,
            "Asia/Tashkent"
        ).formatStartTime(START_MILLIS, "Asia/Tashkent", now)

        assertEquals("Fri 14:05", actual)
    }

    @Test
    fun `Given a start ten days ago then date and time`() {
        val now = START_MILLIS + 10 * DAY

        val actual = formatter(
            true,
            "Asia/Tashkent"
        ).formatStartTime(START_MILLIS, "Asia/Tashkent", now)

        assertEquals("23 Oct 14:05", actual)
    }

    @Test
    fun `Given a range in the device zone then no city`() {
        val actual = formatter(true, "Asia/Tashkent").formatRange(
            START_MILLIS,
            START_MILLIS + HOUR,
            "Asia/Tashkent",
            START_MILLIS + 2 * HOUR
        )

        assertEquals(AttackTimeRangeUiModel("14:05", "15:05"), actual)
    }

    @Test
    fun `Given a range in another zone then the city once, after the end`() {
        val actual = formatter(true, "Asia/Dubai").formatRange(
            START_MILLIS,
            START_MILLIS + HOUR,
            "Asia/Tashkent",
            START_MILLIS + 2 * HOUR
        )

        assertEquals(AttackTimeRangeUiModel("14:05", "15:05 (Tashkent)"), actual)
    }

    @Test
    fun `Given a range that began days ago in another zone then the weekday and the city once`() {
        val actual = formatter(true, "Europe/Berlin").formatRange(
            START_MILLIS,
            START_MILLIS + HOUR,
            "Asia/Tashkent",
            START_MILLIS + 3 * DAY
        )

        assertEquals(AttackTimeRangeUiModel("Fri 14:05", "15:05 (Tashkent)"), actual)
    }

    @Test
    fun `Given the attack zone is a day ahead of the device then no weekday`() {
        // 08:00-09:00 on Fri 23 Oct in Tashkent; it is 23:30 that Friday in Dubai, already Saturday in Tashkent.
        val start = Instant.parse("2026-10-23T03:00:00Z").toEpochMilli()
        val now = Instant.parse("2026-10-23T19:30:00Z").toEpochMilli()

        val actual = formatter(
            true,
            "Asia/Dubai"
        ).formatRange(start, start + HOUR, "Asia/Tashkent", now)

        assertEquals(AttackTimeRangeUiModel("08:00", "09:00 (Tashkent)"), actual)
    }
}

private const val HOUR = 3_600_000L
private const val DAY = 24 * HOUR
