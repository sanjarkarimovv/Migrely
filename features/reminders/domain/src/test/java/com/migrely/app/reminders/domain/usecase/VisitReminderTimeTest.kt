package com.migrely.app.reminders.domain.usecase

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private val TASHKENT = ZoneId.of("Asia/Tashkent")
private val NEW_YORK = ZoneId.of("America/New_York")
private val NOVEMBER_20: LocalDate = LocalDate.of(2026, 11, 20)

private fun at(text: String, zone: ZoneId) = LocalDateTime.parse(text).atZone(zone).toInstant()

/** The visit reminder's time (BRD D168, design D3): 10:00 local time two days before, or on the postponed day. */
class VisitReminderTimeTest {
    private val classUnderTest = VisitReminderTime()

    @Test
    fun `Given a visit when planned then 10 AM two days before, in the phone's zone`() {
        assertEquals(
            at("2026-11-18T10:00", TASHKENT),
            classUnderTest.remindAt(NOVEMBER_20, postponedTo = null, TASHKENT)
        )
    }

    @Test
    fun `Given the phone moved to New York when planned again then 10 AM New York time`() {
        assertEquals(
            at("2026-11-18T10:00", NEW_YORK),
            classUnderTest.remindAt(NOVEMBER_20, postponedTo = null, NEW_YORK)
        )
    }

    @Test
    fun `Given the clocks change the night before when planned then still 10 AM local time`() {
        // New York leaves daylight saving on 2026-11-01.
        val visit = LocalDate.of(2026, 11, 3)

        assertEquals(
            at("2026-11-01T10:00", NEW_YORK),
            classUnderTest.remindAt(visit, postponedTo = null, NEW_YORK)
        )
    }

    @Test
    fun `Given an attack postponed it to the next day when planned then 10 AM that day`() {
        assertEquals(
            at("2026-11-19T10:00", TASHKENT),
            classUnderTest.remindAt(NOVEMBER_20, LocalDate.of(2026, 11, 19), TASHKENT)
        )
    }

    @Test
    fun `Given a postponement left from another date when planned then it is ignored`() {
        assertEquals(
            at("2026-11-18T10:00", TASHKENT),
            classUnderTest.remindAt(NOVEMBER_20, LocalDate.of(2026, 11, 1), TASHKENT)
        )
        assertEquals(
            at("2026-11-18T10:00", TASHKENT),
            classUnderTest.remindAt(NOVEMBER_20, LocalDate.of(2026, 11, 21), TASHKENT)
        )
    }

    @Test
    fun `Given the visit day when its end is asked then local midnight after it`() {
        assertEquals(at("2026-11-21T00:00", TASHKENT), classUnderTest.dayEnd(NOVEMBER_20, TASHKENT))
    }

    @Test
    fun `Given the time a visit is set when too close is asked then from 10 AM 2 days before`() {
        assertFalse(
            classUnderTest.tooClose(NOVEMBER_20, at("2026-11-18T09:59", TASHKENT), TASHKENT)
        )
        assertTrue(classUnderTest.tooClose(NOVEMBER_20, at("2026-11-18T10:00", TASHKENT), TASHKENT))
        assertTrue(classUnderTest.tooClose(NOVEMBER_20, at("2026-11-19T08:00", TASHKENT), TASHKENT))
        assertTrue(classUnderTest.tooClose(NOVEMBER_20, at("2026-11-20T08:00", TASHKENT), TASHKENT))
    }
}
