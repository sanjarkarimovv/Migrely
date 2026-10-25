package com.migrely.app.report.data.mapper

import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

private val AFTERNOON: ZonedDateTime =
    ZonedDateTime.of(2026, 10, 20, 14, 30, 0, 0, ZoneId.of("America/New_York"))

class ReportDateTimeFormatterTest {
    private var locale: Locale = Locale.US
    private var is24Hour = false
    private val asked = mutableListOf<Pair<Locale, String>>()
    private var patterns = mapOf("hm" to "h:mm a", "Hm" to "HH:mm")
    private val classUnderTest = ReportDateTimeFormatter(
        locale = { locale },
        is24HourFormat = { is24Hour },
        bestDateTimePattern = { locale, skeleton ->
            asked += locale to skeleton
            patterns.getValue(skeleton)
        }
    )

    @Test
    fun `Given a phone in the US then weeks start on Sunday`() {
        assertEquals(DayOfWeek.SUNDAY, classUnderTest.firstDayOfWeek())
    }

    @Test
    fun `Given a phone in the UK then weeks start on Monday, read at every call`() {
        classUnderTest.firstDayOfWeek()
        locale = Locale.UK

        assertEquals(DayOfWeek.MONDAY, classUnderTest.firstDayOfWeek())
    }

    @Test
    fun `Given a 12-hour phone then the locale's 12-hour time, kept on one line`() {
        assertEquals("2:30\u00A0PM", classUnderTest.formatTime(AFTERNOON))
        assertEquals(listOf(Locale.US to "hm"), asked)
    }

    @Test
    fun `Given a 24-hour phone then the locale's 24-hour time, read at every call`() {
        classUnderTest.formatTime(AFTERNOON)
        locale = Locale.UK
        is24Hour = true

        assertEquals("14:30", classUnderTest.formatTime(AFTERNOON))
        assertEquals(Locale.UK to "Hm", asked.last())
    }

    @Test
    fun `Given a pattern with a narrow no-break space then it is kept`() {
        patterns = mapOf("hm" to "h:mm\u202Fa")

        assertEquals("2:30\u202FPM", classUnderTest.formatTime(AFTERNOON))
    }
}
