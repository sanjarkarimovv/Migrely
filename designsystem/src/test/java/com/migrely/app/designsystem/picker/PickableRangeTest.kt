package com.migrely.app.designsystem.picker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PickableRangeTest {
    @Test
    fun `Given a lower bound with seconds then its whole minute can be picked`() {
        // 14:05:42 to 16:30 (as epoch millis of a whole day's minutes).
        val range = (14 * 3_600_000L + 5 * 60_000L + 42_000L)..(16 * 3_600_000L + 30 * 60_000L)

        val actual = pickableRange(range)

        assertEquals(14 * 3_600_000L + 5 * 60_000L, actual.first)
        assertEquals(range.last, actual.last)
        assertTrue("14:05:00 is pickable", 14 * 3_600_000L + 5 * 60_000L in actual)
    }

    @Test
    fun `Given a whole-minute lower bound then nothing changes`() {
        val range = 120_000L..600_000L

        assertEquals(range, pickableRange(range))
    }
}
