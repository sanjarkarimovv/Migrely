package com.migrely.app.ui.main

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

private const val FIRST_FINGER = 1L
private const val SECOND_FINGER = 2L
private const val THIRD_FINGER = 3L
private const val LIFT_TIME = 10_000L
private val LIMITS = SecondTapLimits(timeoutMillis = 300, distance = 275f)

private fun at(uptimeMillis: Long, x: Float = 500f, y: Float = 500f) =
    TouchPoint(uptimeMillis, x, y)

private fun SecondTapRule.pressOnANewEntry(pointerId: Long, at: TouchPoint) =
    press(pointerId, at, LIMITS, entryTouched = false)

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
        rule.lift(FIRST_FINGER, at(LIFT_TIME))

        val actual = rule.pressOnANewEntry(SECOND_FINGER, at(LIFT_TIME + gap))

        assertEquals(ignored, actual)
    }
}

class SecondTapRuleTest {
    private val rule = SecondTapRule()

    private fun firstTap() = rule.lift(FIRST_FINGER, at(LIFT_TIME))

    @Test
    fun `Given no lift yet when a press comes then it answers`() {
        assertFalse(rule.pressOnANewEntry(FIRST_FINGER, at(LIFT_TIME)))
    }

    @Test
    fun `Given a press just under the distance when it comes then it is ignored`() {
        firstTap()

        assertTrue(rule.pressOnANewEntry(SECOND_FINGER, at(LIFT_TIME + 100, x = 774f)))
    }

    @Test
    fun `Given a press exactly at the distance when it comes then it answers`() {
        firstTap()

        assertFalse(rule.pressOnANewEntry(SECOND_FINGER, at(LIFT_TIME + 100, x = 775f)))
    }

    @Test
    fun `Given a press over the distance on a diagonal when it comes then it answers`() {
        firstTap()

        assertFalse(rule.pressOnANewEntry(SECOND_FINGER, at(LIFT_TIME + 100, x = 700f, y = 700f)))
    }

    @Test
    fun `Given a touched entry when a press comes close then it answers`() {
        firstTap()

        assertFalse(rule.press(SECOND_FINGER, at(LIFT_TIME + 100), LIMITS, entryTouched = true))
    }

    @Test
    fun `Given a lift of a press that answered when asked then it counts as a touch`() {
        rule.pressOnANewEntry(FIRST_FINGER, at(LIFT_TIME))

        assertTrue(rule.lift(FIRST_FINGER, at(LIFT_TIME + 50)))
    }

    @Test
    fun `Given a lift of an ignored press when asked then it does not count as a touch`() {
        firstTap()
        rule.pressOnANewEntry(SECOND_FINGER, at(LIFT_TIME + 100))

        assertFalse(rule.lift(SECOND_FINGER, at(LIFT_TIME + 130)))
    }

    @Test
    fun `Given an ignored press when it is asked about then it stays ignored until it lifts`() {
        firstTap()
        rule.pressOnANewEntry(SECOND_FINGER, at(LIFT_TIME + 100))

        assertTrue(rule.isIgnored(SECOND_FINGER))

        rule.lift(SECOND_FINGER, at(LIFT_TIME + 5_000))

        assertFalse(rule.isIgnored(SECOND_FINGER))
    }

    @Test
    fun `Given a press that answers when it is asked about then it is not ignored`() {
        rule.pressOnANewEntry(SECOND_FINGER, at(LIFT_TIME))

        assertFalse(rule.isIgnored(SECOND_FINGER))
    }

    @Test
    fun `Given a burst of three when the third comes close then it is ignored too`() {
        firstTap()
        rule.pressOnANewEntry(SECOND_FINGER, at(LIFT_TIME + 100))
        rule.lift(SECOND_FINGER, at(LIFT_TIME + 130))

        assertTrue(rule.pressOnANewEntry(THIRD_FINGER, at(LIFT_TIME + 400)))
    }

    @Test
    fun `Given a burst of three when a pause follows then the next press answers`() {
        firstTap()
        rule.pressOnANewEntry(SECOND_FINGER, at(LIFT_TIME + 100))
        rule.lift(SECOND_FINGER, at(LIFT_TIME + 130))

        assertFalse(rule.pressOnANewEntry(THIRD_FINGER, at(LIFT_TIME + 430)))
    }

    @Test
    fun `Given two fingers when one is ignored then the other is judged by itself`() {
        firstTap()

        assertTrue(rule.pressOnANewEntry(SECOND_FINGER, at(LIFT_TIME + 100)))
        assertFalse(rule.pressOnANewEntry(THIRD_FINGER, at(LIFT_TIME + 110, x = 2_000f)))
        assertTrue(rule.isIgnored(SECOND_FINGER))
        assertFalse(rule.isIgnored(THIRD_FINGER))
    }

    @Test
    fun `Given a stale ignored id when the same id presses later then it decides afresh`() {
        firstTap()
        rule.pressOnANewEntry(SECOND_FINGER, at(LIFT_TIME + 100))

        rule.pressOnANewEntry(SECOND_FINGER, at(LIFT_TIME + 5_000))

        assertFalse(rule.isIgnored(SECOND_FINGER))
    }
}
