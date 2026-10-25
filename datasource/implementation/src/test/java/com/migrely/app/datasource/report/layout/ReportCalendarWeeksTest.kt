package com.migrely.app.datasource.report.layout

import com.migrely.app.datasource.report.model.ReportDayLevelDataModel
import com.migrely.app.datasource.report.model.ReportHeatMonthDataModel
import org.junit.Assert.assertEquals
import org.junit.Test

/** A month of [days] days whose days from [firstDrawn] through [lastDrawn], 1-based, are in the period. */
private fun month(
    leadingEmptyDays: Int,
    days: Int = 31,
    firstDrawn: Int = 1,
    lastDrawn: Int = days
) = ReportHeatMonthDataModel(
    title = "October 2026",
    leadingEmptyDays = leadingEmptyDays,
    levels = List(days) { index ->
        if (index + 1 in firstDrawn..lastDrawn) {
            ReportDayLevelDataModel.Mild
        } else {
            ReportDayLevelDataModel.Outside
        }
    }
)

class ReportCalendarWeeksTest {
    @Test
    fun `Given a whole month then the weeks it spans`() {
        // August 2026 starts on a Saturday: six weeks from Monday, five from Saturday.
        assertEquals(6, weekRowsOf(listOf(month(leadingEmptyDays = 5))))
        assertEquals(5, weekRowsOf(listOf(month(leadingEmptyDays = 0))))
    }

    @Test
    fun `Given a period ending on the 25th then no rows for the days after it`() {
        // 1 October 2026 is a Thursday; the 25th ends the fourth week from Monday, starts the fifth from Sunday.
        assertEquals(4, weekRowsOf(listOf(month(leadingEmptyDays = 3, lastDrawn = 25))))
        assertEquals(5, weekRowsOf(listOf(month(leadingEmptyDays = 4, lastDrawn = 25))))
    }

    @Test
    fun `Given a period starting on the 26th then the weeks before it keep their rows`() {
        assertEquals(5, weekRowsOf(listOf(month(leadingEmptyDays = 2, firstDrawn = 26))))
    }

    @Test
    fun `Given a row of months then the most any of them needs`() {
        // From Monday: the end of October, November 2026, which starts on a Sunday, and February 2027.
        val row = listOf(
            month(leadingEmptyDays = 3, lastDrawn = 25),
            month(leadingEmptyDays = 6, days = 30),
            month(leadingEmptyDays = 0, days = 28)
        )

        assertEquals(6, weekRowsOf(row))
    }

    @Test
    fun `Given nothing drawn then no rows`() {
        assertEquals(0, weekRowsOf(listOf(month(leadingEmptyDays = 3, firstDrawn = 32))))
        assertEquals(0, weekRowsOf(emptyList()))
    }
}
