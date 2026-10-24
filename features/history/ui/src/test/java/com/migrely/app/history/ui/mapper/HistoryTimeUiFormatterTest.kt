package com.migrely.app.history.ui.mapper

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

private val TASHKENT: ZoneId = ZoneId.of("Asia/Tashkent")

private fun millis(local: String, zone: ZoneId = TASHKENT) =
    LocalDateTime.parse(local).atZone(zone).toInstant().toEpochMilli()

class HistoryTimeUiFormatterTest {
    private fun formatter(is24Hour: Boolean = true) = HistoryTimeUiFormatter(
        locale = { Locale.UK },
        is24HourFormat = { is24Hour },
        deviceZone = { TASHKENT },
        bestDateTimePattern = { _, skeleton ->
            when (skeleton) {
                "Hm" -> "HH:mm"
                "hm" -> "h:mm a"
                "MMMMy" -> "MMMM y"
                "MMMM" -> "LLLL"
                "EEEEdMMMM" -> "EEEE, d MMMM"
                "EEEdMMM" -> "EEE d MMM"
                else -> "EEE"
            }
        }
    )

    @Test
    fun `When formatting the month header then month and year`() {
        assertEquals("October 2026", formatter().formatMonthTitle(2026, 10))
    }

    @Test
    fun `When formatting the month name then the month alone`() {
        assertEquals("October", formatter().formatMonthName(2026, 10))
    }

    @Test
    fun `When formatting a day title then weekday, day and month`() {
        assertEquals(
            "Wednesday, 14 October",
            formatter().formatDayTitle(LocalDate.parse("2026-10-14").toEpochDay())
        )
    }

    @Test
    fun `When formatting a start then the short day and the time`() {
        assertEquals(
            "Wed 14 Oct, 09:10",
            formatter().formatStart(millis("2026-10-14T09:10"), "Asia/Tashkent")
        )
    }

    @Test
    fun `When formatting a spoken start then the full day`() {
        assertEquals(
            "Wednesday, 14 October, 09:10",
            formatter().formatStart(millis("2026-10-14T09:10"), "Asia/Tashkent", spoken = true)
        )
    }

    @Test
    fun `Given the same day when formatting an end then the time alone`() {
        assertEquals(
            "10:00",
            formatter().formatEnd(
                millis("2026-10-14T10:00"),
                millis("2026-10-14T09:10"),
                "Asia/Tashkent"
            )
        )
    }

    @Test
    fun `Given the next day when formatting an end then its weekday too`() {
        assertEquals(
            "Thu 03:00",
            formatter().formatEnd(
                millis("2026-10-15T03:00"),
                millis("2026-10-14T22:00"),
                "Asia/Tashkent"
            )
        )
    }

    @Test
    fun `Given 12-hour format when formatting a time then am-pm`() {
        assertEquals(
            "2:05 pm",
            formatter(is24Hour = false).formatClockTime(millis("2026-10-14T14:05"), "Asia/Tashkent")
        )
    }

    @Test
    fun `Given another zone when formatting a time then its city`() {
        assertEquals(
            "08:00 (Tokyo)",
            formatter().formatClockTime(
                millis("2026-10-15T08:00", ZoneId.of("Asia/Tokyo")),
                "Asia/Tokyo"
            )
        )
    }
}
