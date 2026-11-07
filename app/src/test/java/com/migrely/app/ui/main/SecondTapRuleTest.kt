package com.migrely.app.ui.main

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

private const val OLD_SCREEN = "old-screen"
private const val NEW_SCREEN = "new-screen"
private const val FIRST_FINGER = 1L
private const val SECOND_FINGER = 2L
private const val THIRD_FINGER = 3L
private const val LIFT_TIME = 10_000L
private val LIMITS = SecondTapLimits(timeoutMillis = 300, distance = 275f)

private fun at(uptimeMillis: Long, x: Float = 500f, y: Float = 500f) =
    TouchPoint(uptimeMillis, x, y)

/** The gap from the lift of the first tap to the press of the second, at the same point. */
@RunWith(Parameterized::class)
class SecondTapRuleGapTest(private val gap: Long, private val ignored: Boolean) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} ms ignored: {1}")
        fun parameters() = listOf(
            arrayOf<Any>(0L, true),
            arrayOf<Any>(39L, true),
            arrayOf<Any>(40L, true),
            arrayOf<Any>(60L, true),
            arrayOf<Any>(100L, true),
            arrayOf<Any>(150L, true),
            arrayOf<Any>(299L, true),
            arrayOf<Any>(300L, false),
            arrayOf<Any>(301L, false),
            arrayOf<Any>(700L, false)
        )
    }

    @Test
    fun `Given a lift on the screen before when a press comes at this gap then the rule says`() {
        val rule = SecondTapRule()
        rule.lift(OLD_SCREEN, FIRST_FINGER, at(LIFT_TIME))

        val actual = rule.press(NEW_SCREEN, SECOND_FINGER, at(LIFT_TIME + gap), LIMITS)

        assertEquals(ignored, actual)
    }
}

class SecondTapRuleTest {
    private val rule = SecondTapRule()

    private fun tapOnOldScreen() = rule.lift(OLD_SCREEN, FIRST_FINGER, at(LIFT_TIME))

    @Test
    fun `Given no lift yet when a press comes then it answers`() {
        assertFalse(rule.press(NEW_SCREEN, FIRST_FINGER, at(LIFT_TIME), LIMITS))
    }

    @Test
    fun `Given a press just under the distance when it comes then it is ignored`() {
        tapOnOldScreen()

        assertTrue(rule.press(NEW_SCREEN, SECOND_FINGER, at(LIFT_TIME + 100, x = 774f), LIMITS))
    }

    @Test
    fun `Given a press exactly at the distance when it comes then it answers`() {
        tapOnOldScreen()

        assertFalse(rule.press(NEW_SCREEN, SECOND_FINGER, at(LIFT_TIME + 100, x = 775f), LIMITS))
    }

    @Test
    fun `Given a press over the distance on a diagonal when it comes then it answers`() {
        tapOnOldScreen()

        val press = at(LIFT_TIME + 100, x = 700f, y = 700f)

        assertFalse(rule.press(NEW_SCREEN, SECOND_FINGER, press, LIMITS))
    }

    @Test
    fun `Given a screen a touch has lifted on when a press comes close then it answers`() {
        rule.lift(NEW_SCREEN, FIRST_FINGER, at(LIFT_TIME - 5_000))
        tapOnOldScreen()

        assertFalse(rule.press(NEW_SCREEN, SECOND_FINGER, at(LIFT_TIME + 100), LIMITS))
    }

    @Test
    fun `Given a tap on this screen when a second tap comes on it then both count`() {
        rule.lift(NEW_SCREEN, FIRST_FINGER, at(LIFT_TIME))

        assertFalse(rule.press(NEW_SCREEN, SECOND_FINGER, at(LIFT_TIME + 100), LIMITS))
    }

    @Test
    fun `Given an ignored press when it is asked about then it stays ignored until it lifts`() {
        tapOnOldScreen()
        rule.press(NEW_SCREEN, SECOND_FINGER, at(LIFT_TIME + 100), LIMITS)

        assertTrue(rule.isIgnored(SECOND_FINGER))

        rule.lift(NEW_SCREEN, SECOND_FINGER, at(LIFT_TIME + 5_000))

        assertFalse(rule.isIgnored(SECOND_FINGER))
    }

    @Test
    fun `Given a press that answers when it is asked about then it is not ignored`() {
        rule.press(NEW_SCREEN, SECOND_FINGER, at(LIFT_TIME), LIMITS)

        assertFalse(rule.isIgnored(SECOND_FINGER))
    }

    @Test
    fun `Given a burst of three when the third comes close then it is ignored too`() {
        tapOnOldScreen()
        rule.press(NEW_SCREEN, SECOND_FINGER, at(LIFT_TIME + 100), LIMITS)
        rule.lift(NEW_SCREEN, SECOND_FINGER, at(LIFT_TIME + 130))

        assertTrue(rule.press(NEW_SCREEN, THIRD_FINGER, at(LIFT_TIME + 400), LIMITS))
    }

    @Test
    fun `Given a burst of three when a pause follows then the next press answers`() {
        tapOnOldScreen()
        rule.press(NEW_SCREEN, SECOND_FINGER, at(LIFT_TIME + 100), LIMITS)
        rule.lift(NEW_SCREEN, SECOND_FINGER, at(LIFT_TIME + 130))

        assertFalse(rule.press(NEW_SCREEN, THIRD_FINGER, at(LIFT_TIME + 430), LIMITS))
    }

    @Test
    fun `Given an ignored lift when a later press comes close then the entry is still new`() {
        tapOnOldScreen()
        rule.press(NEW_SCREEN, SECOND_FINGER, at(LIFT_TIME + 100), LIMITS)
        rule.lift(NEW_SCREEN, SECOND_FINGER, at(LIFT_TIME + 130))
        rule.lift(OLD_SCREEN, FIRST_FINGER, at(LIFT_TIME + 5_000))

        assertTrue(rule.press(NEW_SCREEN, THIRD_FINGER, at(LIFT_TIME + 5_100), LIMITS))
    }

    @Test
    fun `Given two fingers when one is ignored then the other is judged by itself`() {
        tapOnOldScreen()

        assertTrue(rule.press(NEW_SCREEN, SECOND_FINGER, at(LIFT_TIME + 100), LIMITS))
        assertFalse(rule.press(NEW_SCREEN, THIRD_FINGER, at(LIFT_TIME + 110, x = 2_000f), LIMITS))
        assertTrue(rule.isIgnored(SECOND_FINGER))
        assertFalse(rule.isIgnored(THIRD_FINGER))
    }

    @Test
    fun `Given a cancelled press when presses are forgotten then it is not ignored`() {
        tapOnOldScreen()
        rule.press(NEW_SCREEN, SECOND_FINGER, at(LIFT_TIME + 100), LIMITS)

        rule.forgetPresses()

        assertFalse(rule.isIgnored(SECOND_FINGER))
    }

    @Test
    fun `Given a return to a touched screen when a press comes close then it answers`() {
        rule.lift(OLD_SCREEN, FIRST_FINGER, at(LIFT_TIME - 5_000))
        rule.lift(NEW_SCREEN, SECOND_FINGER, at(LIFT_TIME))

        assertFalse(rule.press(OLD_SCREEN, THIRD_FINGER, at(LIFT_TIME + 100), LIMITS))
    }
}
