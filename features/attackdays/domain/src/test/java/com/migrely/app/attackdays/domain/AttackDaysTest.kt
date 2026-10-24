package com.migrely.app.attackdays.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

private val TASHKENT = ZoneId.of("Asia/Tashkent")
private val NEW_YORK = ZoneId.of("America/New_York")
private val NOW = Instant.parse("2026-10-24T10:00:00Z")

class AttackDaysTest {
    private fun date(text: String) = LocalDate.parse(text)

    private fun at(text: String) = Instant.parse(text)

    @Test
    fun `Given an attack within a day when reading its dates then that day`() {
        assertEquals(
            date("2026-10-20")..date("2026-10-20"),
            AttackDays.datesTouched(
                at("2026-10-20T04:00:00Z"),
                TASHKENT,
                at("2026-10-20T09:00:00Z"),
                NOW
            )
        )
    }

    @Test
    fun `Given an attack past midnight when reading its dates then both days`() {
        // 22:00 to 03:00 in Tashkent (UTC+5).
        assertEquals(
            date("2026-08-31")..date("2026-09-01"),
            AttackDays.datesTouched(
                at("2026-08-31T17:00:00Z"),
                TASHKENT,
                at("2026-08-31T22:00:00Z"),
                NOW
            )
        )
    }

    @Test
    fun `Given an attack ending exactly at midnight then not the next day`() {
        assertEquals(
            date("2026-10-20")..date("2026-10-20"),
            AttackDays.datesTouched(
                at("2026-10-20T10:00:00Z"),
                TASHKENT,
                at("2026-10-20T19:00:00Z"),
                NOW
            )
        )
    }

    @Test
    fun `Given an attack in progress when reading its dates then up to now`() {
        assertEquals(
            date("2026-10-22")..date("2026-10-24"),
            AttackDays.datesTouched(at("2026-10-22T10:00:00Z"), TASHKENT, null, NOW)
        )
    }

    @Test
    fun `Given an attack logged in another zone when reading its dates then that zone's dates`() {
        // 20:00 on 23 October in New York is already 24 October in Tashkent.
        assertEquals(
            date("2026-10-23")..date("2026-10-23"),
            AttackDays.datesTouched(
                at("2026-10-24T00:00:00Z"),
                NEW_YORK,
                at("2026-10-24T02:00:00Z"),
                NOW
            )
        )
    }

    @Test
    fun `When reading the window then two days on each side of the dates in the zone`() {
        assertEquals(
            at("2026-10-01T19:00:00Z"),
            AttackDays.windowStart(date("2026-10-04"), TASHKENT)
        )
        assertEquals(at("2026-10-06T19:00:00Z"), AttackDays.windowEnd(date("2026-10-04"), TASHKENT))
    }
}
