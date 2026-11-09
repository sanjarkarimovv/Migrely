package com.migrely.app.reminders.domain.usecase

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

private val TODAY: LocalDate = LocalDate.of(2026, 11, 18)
private val THREE_MONTHS: LocalDate = LocalDate.of(2026, 8, 19)

/**
 * The period a report covers by default (BRD EC-REP-04, design D4). The report feature runs the same table in its own
 * `DefaultReportPeriodTest`, so the two copies of the rule cannot drift apart unnoticed.
 */
@RunWith(Parameterized::class)
class DefaultReportPeriodTest(
    private val case: String,
    private val lastVisit: LocalDate?,
    private val firstDay: LocalDate
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases() = listOf(
            arrayOf<Any?>("no visit", null, THREE_MONTHS),
            arrayOf<Any?>("24 months ago to the day", TODAY.minusMonths(24), TODAY.minusMonths(24)),
            arrayOf<Any?>(
                "a day older than that",
                TODAY.minusMonths(24).minusDays(1),
                THREE_MONTHS
            ),
            arrayOf<Any?>("ten days ago", TODAY.minusDays(10), TODAY.minusDays(10)),
            arrayOf<Any?>("yesterday", TODAY.minusDays(1), TODAY.minusDays(1))
        )
    }

    @Test
    fun `Given the last visit when the default period is asked then its first day`() {
        assertEquals(case, firstDay, DefaultReportPeriod.firstDay(lastVisit, TODAY))
    }
}
