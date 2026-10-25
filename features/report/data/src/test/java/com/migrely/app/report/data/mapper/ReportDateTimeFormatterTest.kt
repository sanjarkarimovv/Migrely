package com.migrely.app.report.data.mapper

import java.time.DayOfWeek
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class ReportDateTimeFormatterTest {
    private var locale: Locale = Locale.US
    private val classUnderTest = ReportDateTimeFormatter { locale }

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
}
