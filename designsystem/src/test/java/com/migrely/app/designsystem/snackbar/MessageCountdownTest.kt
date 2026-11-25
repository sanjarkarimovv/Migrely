package com.migrely.app.designsystem.snackbar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private const val UNDO_MILLIS = 5_000L
private const val READING_MILLIS = 8_000L
private const val TIME_TO_TAKE_ACTION_MILLIS = 120_000L
private const val HOUR_MILLIS = 3_600_000L
private const val LINE_DELTA = 0.001f

private val NOTHING = CountdownHold()
private val KEYBOARD = CountdownHold(keyboardFocus = true)
private val TALKBACK = CountdownHold(screenReaderFocus = true, screenReaderOn = true)

/** Both clocks of a phone, moved by the test. */
private class FakeClock : MessageClock {
    var wall = 1_793_161_943_447L
    var elapsed = 50_000_000L

    override fun wallMillis(): Long = wall

    override fun elapsedRealtimeMillis(): Long = elapsed

    fun pass(millis: Long) {
        wall += millis
        elapsed += millis
    }
}

/** The countdown of ux §B2: P1–P7, the clocks, the saved state and the line (BRD D182 (2), (3), EC-UX-53…58). */
class MessageCountdownTest {
    private val clock = FakeClock()

    private fun undo(timeoutMillis: Long = UNDO_MILLIS, actionWallMillis: Long = clock.wall) =
        MessageCountdown.start(clock, timeoutMillis, undoActionWallMillis = actionWallMillis)

    private fun message(timeoutMillis: Long?) =
        MessageCountdown.start(clock, timeoutMillis, undoActionWallMillis = null)

    private fun MessageCountdown.assertLeft(millis: Long) = assertEquals(millis, remainingMillis())

    // Keyboard (P1, P2, EC-UX-53)

    @Test
    fun `P1 keyboard holds Undo, and it leaves with the time it had left`() {
        val countdown = undo()
        clock.pass(1_000)
        countdown.update(KEYBOARD)
        clock.pass(30_000)
        assertTrue(countdown.isPaused())
        assertFalse(countdown.isExpired())

        countdown.update(NOTHING)
        clock.pass(299)
        assertTrue(countdown.isPaused())
        clock.pass(1)
        assertFalse(countdown.isPaused())
        countdown.assertLeft(4_000)
        clock.pass(3_999)
        assertFalse(countdown.isExpired())
        clock.pass(1)
        assertTrue(countdown.isExpired())
    }

    @Test
    fun `P2 focus leaving with 0_5 s left gives 3 s`() {
        val countdown = undo()
        clock.pass(4_500)
        countdown.update(KEYBOARD)
        clock.pass(10_000)

        countdown.update(NOTHING)
        clock.pass(300)

        countdown.assertLeft(3_000)
        clock.pass(2_999)
        assertFalse(countdown.isExpired())
        clock.pass(1)
        assertTrue(countdown.isExpired())
    }

    @Test
    fun `Focus back within the grace keeps the pause, and neither counts as shown time`() {
        val countdown = undo(timeoutMillis = 10_000)
        clock.pass(3_000)
        countdown.update(KEYBOARD)
        clock.pass(1_000)
        countdown.update(NOTHING)
        clock.pass(200)
        countdown.update(KEYBOARD)
        clock.pass(5_000)
        assertTrue(countdown.isPaused())

        countdown.update(NOTHING)
        clock.pass(300)

        countdown.assertLeft(7_000)
    }

    // TalkBack (P4, EC-UX-54, EC-UX-55)

    @Test
    fun `P4 TalkBack moving from the text to the control does not resume between them`() {
        val countdown = undo()
        countdown.update(TALKBACK)
        clock.pass(2_000)
        countdown.update(TALKBACK.copy(screenReaderFocus = false))
        clock.pass(50)
        countdown.update(TALKBACK)
        clock.pass(10_000)

        assertTrue(countdown.isPaused())
        countdown.assertLeft(UNDO_MILLIS)
    }

    @Test
    fun `TalkBack moving on resumes after the grace`() {
        val countdown = message(READING_MILLIS)
        countdown.update(TALKBACK)
        countdown.update(TALKBACK.copy(screenReaderFocus = false))

        clock.pass(299)
        assertTrue(countdown.isPaused())
        clock.pass(1)
        assertFalse(countdown.isPaused())
    }

    @Test
    fun `TalkBack turned off counts as focus leaving`() {
        val countdown = message(READING_MILLIS)
        countdown.update(TALKBACK)
        clock.pass(1_000)

        countdown.update(TALKBACK.copy(screenReaderOn = false))
        clock.pass(300)

        assertFalse(countdown.isPaused())
        countdown.assertLeft(READING_MILLIS)
    }

    @Test
    fun `Screen reader focus holds nothing while TalkBack is off`() {
        val countdown = message(READING_MILLIS)

        countdown.update(CountdownHold(screenReaderFocus = true))

        assertFalse(countdown.isPaused())
    }

    // The window (P5, EC-UX-55)

    @Test
    fun `P5 the window losing focus resumes at once with the floor`() {
        val countdown = message(READING_MILLIS)
        clock.pass(7_000)
        countdown.update(KEYBOARD)
        clock.pass(10_000)

        countdown.update(KEYBOARD.copy(windowFocused = false))

        assertFalse(countdown.isPaused())
        countdown.assertLeft(3_000)
    }

    @Test
    fun `The window coming back with focus still on the control pauses again`() {
        val countdown = message(READING_MILLIS)
        countdown.update(KEYBOARD)
        countdown.update(KEYBOARD.copy(windowFocused = false))
        clock.pass(1_000)

        countdown.update(KEYBOARD)
        clock.pass(10_000)

        assertTrue(countdown.isPaused())
        countdown.assertLeft(7_000)
    }

    // Replacement and storage full (P6, P7, EC-UX-56)

    @Test
    fun `P6 a new message runs its own full, unpaused time`() {
        undo().update(KEYBOARD)
        clock.pass(10_000)

        val next = message(READING_MILLIS)

        assertFalse(next.isPaused())
        next.assertLeft(READING_MILLIS)
    }

    @Test
    fun `P7 a message with no duration never ends and has nothing to run down`() {
        val countdown = message(timeoutMillis = null)
        countdown.update(KEYBOARD)
        countdown.update(NOTHING)
        clock.pass(HOUR_MILLIS)

        assertNull(countdown.remainingMillis())
        assertFalse(countdown.isExpired())
        assertEquals(1f, countdown.timeLeftFraction(), LINE_DELTA)
    }

    // The ceiling (P3, EC-UX-58, EC-ATK-24)

    @Test
    fun `P3 a held deletion Undo leaves 115 s after the deletion`() {
        val deletedAt = clock.wall
        clock.pass(25_000)
        val countdown = undo(TIME_TO_TAKE_ACTION_MILLIS, actionWallMillis = deletedAt)
        countdown.update(KEYBOARD)

        clock.pass(89_999)
        assertFalse(countdown.isExpired())
        clock.pass(1)
        assertTrue(countdown.isExpired())
    }

    @Test
    fun `A deletion offered 25 s late leaves at 115 s, not 145 s`() {
        val deletedAt = clock.wall
        clock.pass(25_000)

        val countdown = undo(TIME_TO_TAKE_ACTION_MILLIS, actionWallMillis = deletedAt)

        countdown.assertLeft(90_000)
    }

    @Test
    fun `The floor never passes the ceiling`() {
        val countdown = undo(TIME_TO_TAKE_ACTION_MILLIS)
        countdown.update(KEYBOARD)
        clock.pass(114_000)

        countdown.update(NOTHING)
        clock.pass(300)

        countdown.assertLeft(700)
    }

    @Test
    fun `A clock set back an hour during a pause leaves 115 s after the Undo appeared`() {
        val countdown = undo(TIME_TO_TAKE_ACTION_MILLIS)
        countdown.update(KEYBOARD)
        clock.pass(10_000)
        clock.wall -= HOUR_MILLIS

        clock.pass(104_999)
        assertFalse(countdown.isExpired())
        clock.pass(1)
        assertTrue(countdown.isExpired())
    }

    @Test
    fun `A clock set forward past the ceiling ends the Undo`() {
        val countdown = undo(TIME_TO_TAKE_ACTION_MILLIS)
        clock.pass(1_000)

        clock.wall += 10 * 60_000L

        assertTrue(countdown.isExpired())
    }

    // The setting changes while shown (EC-UX-52)

    @Test
    fun `A new timeout counts against the unpaused time already shown`() {
        val countdown = message(UNDO_MILLIS)
        clock.pass(1_000)
        countdown.update(KEYBOARD)
        clock.pass(10_000)
        countdown.update(NOTHING)
        clock.pass(300)

        countdown.timeoutChanged(20_000)

        countdown.assertLeft(19_000)
    }

    // Saved state (EC-UX-57)

    @Test
    fun `Rotating while paused ends the pause at the save with about 3 s back`() {
        val countdown = undo()
        clock.pass(3_000)
        countdown.update(KEYBOARD)
        clock.pass(20_000)

        val saved = countdown.save()
        clock.pass(200)
        val restored = MessageCountdown.restore(clock, UNDO_MILLIS, saved)

        assertNotNull(restored)
        assertFalse(restored!!.isPaused())
        restored.assertLeft(2_800)
        assertEquals(0.4f, restored.timeLeftFraction(), LINE_DELTA)
    }

    @Test
    fun `Saving leaves the live countdown as it was`() {
        val countdown = undo()
        clock.pass(3_000)
        countdown.update(KEYBOARD)

        countdown.save()
        clock.pass(20_000)

        assertTrue(countdown.isPaused())
        countdown.assertLeft(2_000)
    }

    @Test
    fun `Rotating while running keeps the time left with no floor`() {
        val countdown = undo()
        clock.pass(2_000)

        val saved = countdown.save()
        clock.pass(100)

        MessageCountdown.restore(clock, UNDO_MILLIS, saved)!!.assertLeft(2_900)
    }

    @Test
    fun `Process death while paused restores the time left at the save, less the wait`() {
        val countdown = message(20_000)
        clock.pass(5_000)
        countdown.update(KEYBOARD)
        clock.pass(60_000)

        val saved = countdown.save()
        clock.pass(4_000)

        MessageCountdown.restore(clock, 20_000, saved)!!.assertLeft(11_000)
    }

    @Test
    fun `A restore after the time left at the save has run out shows nothing, even if paused`() {
        val countdown = message(20_000)
        clock.pass(5_000)
        countdown.update(KEYBOARD)
        val saved = countdown.save()

        clock.pass(14_999)
        assertNotNull(MessageCountdown.restore(clock, 20_000, saved))
        clock.pass(1)
        assertNull(MessageCountdown.restore(clock, 20_000, saved))
    }

    @Test
    fun `A restore past the ceiling shows no Undo`() {
        val deletedAt = clock.wall
        clock.pass(20_000)
        val saved = undo(TIME_TO_TAKE_ACTION_MILLIS, actionWallMillis = deletedAt).save()

        clock.pass(94_999)
        assertNotNull(MessageCountdown.restore(clock, TIME_TO_TAKE_ACTION_MILLIS, saved))
        clock.pass(1)
        assertNull(MessageCountdown.restore(clock, TIME_TO_TAKE_ACTION_MILLIS, saved))
    }

    @Test
    fun `An elapsed clock below the saved stamp means a restart, and nothing shows`() {
        val saved = message(READING_MILLIS).save()

        clock.elapsed = 10_000L

        assertNull(MessageCountdown.restore(clock, READING_MILLIS, saved))
    }

    // The time-left line (EC-UX-53)

    @Test
    fun `The line stops while paused and goes on from where it stopped`() {
        val countdown = message(10_000)
        clock.pass(2_000)
        assertEquals(0.8f, countdown.timeLeftFraction(), LINE_DELTA)

        countdown.update(KEYBOARD)
        clock.pass(5_000)
        assertEquals(0.8f, countdown.timeLeftFraction(), LINE_DELTA)

        countdown.update(NOTHING)
        clock.pass(300)
        assertEquals(0.8f, countdown.timeLeftFraction(), LINE_DELTA)
        clock.pass(4_000)
        assertEquals(0.4f, countdown.timeLeftFraction(), LINE_DELTA)
    }

    @Test
    fun `The line never grows when the floor applies, and reaches 0 at the end`() {
        val countdown = undo()
        clock.pass(4_500)
        countdown.update(KEYBOARD)
        clock.pass(1_000)

        countdown.update(NOTHING)
        clock.pass(300)
        assertEquals(0.1f, countdown.timeLeftFraction(), LINE_DELTA)
        clock.pass(1_500)
        assertEquals(0.05f, countdown.timeLeftFraction(), LINE_DELTA)
        clock.pass(1_500)
        assertEquals(0f, countdown.timeLeftFraction(), LINE_DELTA)
        assertTrue(countdown.isExpired())
    }

    @Test
    fun `The line meets the ceiling`() {
        val deletedAt = clock.wall
        clock.pass(25_000)
        val countdown = undo(TIME_TO_TAKE_ACTION_MILLIS, actionWallMillis = deletedAt)

        clock.pass(45_000)
        assertEquals(0.5f, countdown.timeLeftFraction(), LINE_DELTA)
        clock.pass(44_999)
        assertTrue(countdown.timeLeftFraction() > 0f)
        clock.pass(1)
        assertEquals(0f, countdown.timeLeftFraction(), LINE_DELTA)
        assertTrue(countdown.isExpired())
    }

    // When to look again (the driver of rememberMessageCountdown)

    @Test
    fun `The next check is the real end, the grace's end, or the ceiling while held`() {
        val countdown = undo(TIME_TO_TAKE_ACTION_MILLIS)
        assertEquals(UNDO_CEILING_MILLIS, countdown.nextCheckInMillis())

        countdown.update(KEYBOARD)
        clock.pass(5_000)
        assertEquals(110_000L, countdown.nextCheckInMillis())

        countdown.update(NOTHING)
        clock.pass(100)
        assertEquals(200L, countdown.nextCheckInMillis())
    }

    @Test
    fun `A held message with no Undo has no next check until its hold changes`() {
        val countdown = message(READING_MILLIS)

        countdown.update(KEYBOARD)

        assertNull(countdown.nextCheckInMillis())
    }
}
