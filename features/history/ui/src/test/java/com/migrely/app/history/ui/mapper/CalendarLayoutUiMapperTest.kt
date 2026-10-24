package com.migrely.app.history.ui.mapper

import com.migrely.app.history.ui.model.CalendarLayoutUiModel
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarLayoutUiMapperTest {
    @Test
    fun `Given a UK locale when laying out October 2026 then Monday first and three blanks`() {
        // 1 October 2026 is a Thursday.
        assertEquals(
            CalendarLayoutUiModel(listOf("M", "T", "W", "T", "F", "S", "S"), 3),
            CalendarLayoutUiMapper { Locale.UK }.toUi(2026, 10)
        )
    }

    @Test
    fun `Given a US locale when laying out October 2026 then Sunday first and four blanks`() {
        assertEquals(
            CalendarLayoutUiModel(listOf("S", "M", "T", "W", "T", "F", "S"), 4),
            CalendarLayoutUiMapper { Locale.US }.toUi(2026, 10)
        )
    }

    @Test
    fun `Given a month starting on the first weekday when laying out then no blanks`() {
        // 1 June 2026 is a Monday.
        assertEquals(0, CalendarLayoutUiMapper { Locale.UK }.toUi(2026, 6).leadingEmptyDays)
    }
}
