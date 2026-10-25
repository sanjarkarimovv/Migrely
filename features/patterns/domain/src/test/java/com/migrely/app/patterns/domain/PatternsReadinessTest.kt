package com.migrely.app.patterns.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private val TODAY: LocalDate = LocalDate.parse("2026-10-25")

class PatternsReadinessTest {
    private fun daysAgo(vararg days: Long) = days.map { TODAY.minusDays(it) }

    @Test
    fun `Given 5 migraines in 6 months, the first 30 days ago, then patterns show`() {
        assertTrue(PatternsReadiness.ready(daysAgo(30, 20, 10, 5, 1), TODAY))
    }

    @Test
    fun `Given 5 migraines but the first only 29 days ago then they wait a day`() {
        assertFalse(PatternsReadiness.ready(daysAgo(29, 20, 10, 5, 1), TODAY))
        assertEquals(1, PatternsReadiness.missingDays(TODAY.minusDays(29), TODAY))
    }

    @Test
    fun `Given 5 migraines with one older than 6 months then one is missing`() {
        val dates = daysAgo(200, 40, 20, 10, 1)

        assertFalse(PatternsReadiness.ready(dates, TODAY))
        assertEquals(LocalDate.parse("2026-04-26")..TODAY, PatternsReadiness.lastMonths(6, TODAY))
    }

    @Test
    fun `Given nothing logged then 5 migraines and 30 days are missing`() {
        assertEquals(5, PatternsReadiness.missingMigraines(0))
        assertEquals(30, PatternsReadiness.missingDays(null, TODAY))
        assertFalse(PatternsReadiness.ready(emptyList(), TODAY))
    }
}
