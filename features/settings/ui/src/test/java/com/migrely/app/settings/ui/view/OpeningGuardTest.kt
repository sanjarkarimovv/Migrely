package com.migrely.app.settings.ui.view

import org.junit.Assert.assertEquals
import org.junit.Test

private const val DOUBLE_TAP_TIMEOUT = 300L

/** One opening per tap for Share and Rate (BRD EC-SHR-05, design D1). */
class OpeningGuardTest {
    private var now = 1_000L
    private val classUnderTest = OpeningGuard(DOUBLE_TAP_TIMEOUT) { now }

    /** How many of [taps], given as times, open something. */
    private fun opened(vararg taps: Long): Int = taps.count { at ->
        now = at
        classUnderTest.tryOpen()
    }

    @Test
    fun `Given two taps within the double-tap timeout, on one row or on both, then one opens`() {
        assertEquals(1, opened(1_000L, 1_299L))
    }

    @Test
    fun `Given Settings never left, as when nothing could open, then a tap after the timeout opens again`() {
        // So a deliberate second tap shows and speaks the failure again (D178).
        assertEquals(2, opened(1_000L, 1_300L))
    }

    @Test
    fun `Given Settings left for the share sheet then no tap counts until it is back`() {
        opened(1_000L)
        classUnderTest.onPause()

        assertEquals(0, opened(1_100L, 5_000L))
        classUnderTest.onResume()
        assertEquals(1, opened(5_001L))
    }

    @Test
    fun `Given a pause and resume with nothing opened then the rows are not held`() {
        classUnderTest.onPause()
        classUnderTest.onResume()

        assertEquals(1, opened(1_000L))
    }

    @Test
    fun `Without the guard's wait the same taps open twice (negative control)`() {
        val noWait = OpeningGuard(timeoutMillis = 0L) { now }

        val count = listOf(1_000L, 1_001L).count { at ->
            now = at
            noWait.tryOpen()
        }

        assertEquals(2, count)
    }
}
