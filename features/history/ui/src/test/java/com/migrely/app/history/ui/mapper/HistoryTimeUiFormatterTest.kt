package com.migrely.app.history.ui.mapper

import com.migrely.app.history.ui.model.HistoryTimeRangeUiModel
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
    fun `When formatting an ended range then the short day, the start and the end`() {
        assertEquals(
            HistoryTimeRangeUiModel("Wed 14 Oct, 09:10", "10:00"),
            formatter().formatRange(
                millis("2026-10-14T09:10"),
                millis("2026-10-14T10:00"),
                "Asia/Tashkent"
            )
        )
    }

    @Test
    fun `When formatting a spoken range then the full day`() {
        assertEquals(
            "Wednesday, 14 October, 09:10",
            formatter().formatRange(
                millis("2026-10-14T09:10"),
                millis("2026-10-14T10:00"),
                "Asia/Tashkent",
                spoken = true
            ).start
        )
    }

    @Test
    fun `Given the next day when formatting a range then the end has its weekday too`() {
        assertEquals(
            "Thu 03:00",
            formatter().formatRange(
                millis("2026-10-14T22:00"),
                millis("2026-10-15T03:00"),
                "Asia/Tashkent"
            ).end
        )
    }

    @Test
    fun `Given an attack in progress when formatting a range then no end`() {
        assertEquals(
            HistoryTimeRangeUiModel("Wed 14 Oct, 09:10", null),
            formatter().formatRange(millis("2026-10-14T09:10"), null, "Asia/Tashkent")
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

    @Test
    fun `Given another zone when formatting an ended range then the city once, after the end`() {
        val tokyo = ZoneId.of("Asia/Tokyo")
        assertEquals(
            HistoryTimeRangeUiModel("Thu 15 Oct, 08:00", "09:00 (Tokyo)"),
            formatter().formatRange(
                millis("2026-10-15T08:00", tokyo),
                millis("2026-10-15T09:00", tokyo),
                "Asia/Tokyo"
            )
        )
    }

    @Test
    fun `Given another zone when formatting a range in progress then the city on the start`() {
        assertEquals(
            HistoryTimeRangeUiModel("Thu 15 Oct, 08:00 (Tokyo)", null),
            formatter().formatRange(
                millis("2026-10-15T08:00", ZoneId.of("Asia/Tokyo")),
                null,
                "Asia/Tokyo"
            )
        )
    }
}
