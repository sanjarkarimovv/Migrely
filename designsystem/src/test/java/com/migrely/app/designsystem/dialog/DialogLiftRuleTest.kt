package com.migrely.app.designsystem.dialog

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

private const val TIMEOUT = 300L
private const val DISTANCE = 300f
private const val LIFTED_AT = 1_000L
private val POINT = Offset(500f, 900f)
private val DIALOG = Any()
private val OLDER_DIALOG = Any()

private fun DialogLiftRule.mainPress(pointer: Long, at: Long, position: Offset = POINT) =
    press(null, pointer, at, position, TIMEOUT, DISTANCE)

@RunWith(Parameterized::class)
class DialogLiftRuleGapTest(private val gap: Long, private val expected: Boolean) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} ms after the lift, ignored {1}")
        fun cases() = listOf(
            arrayOf<Any>(0L, true),
            arrayOf<Any>(39L, true),
            arrayOf<Any>(100L, true),
            arrayOf<Any>(150L, true),
            arrayOf<Any>(299L, true),
            arrayOf<Any>(300L, false),
            arrayOf<Any>(301L, false),
            arrayOf<Any>(700L, false)
        )
    }

    @Test
    fun aPressThisLongAfterALiftOnADialog() {
        val rule = DialogLiftRule()
        rule.lift(DIALOG, 1, LIFTED_AT, POINT)

        assertEquals(expected, rule.mainPress(2, LIFTED_AT + gap))
    }
}

class DialogLiftRuleTest {
    private val rule = DialogLiftRule()

    @Test
    fun withNoLiftYetAPressAnswers() {
        assertFalse(rule.mainPress(1, LIFTED_AT))
    }

    @Test
    fun justUnderTheDistanceIsIgnored() {
        rule.lift(DIALOG, 1, LIFTED_AT, POINT)

        assertTrue(rule.mainPress(2, LIFTED_AT + 100, POINT + Offset(DISTANCE - 1f, 0f)))
    }

    @Test
    fun atTheDistanceAnswers() {
        rule.lift(DIALOG, 1, LIFTED_AT, POINT)

        assertFalse(rule.mainPress(2, LIFTED_AT + 100, POINT + Offset(0f, DISTANCE)))
    }

    @Test
    fun theDistanceIsAcrossBothAxes() {
        rule.lift(DIALOG, 1, LIFTED_AT, POINT)

        assertFalse(rule.mainPress(2, LIFTED_AT + 100, POINT + Offset(250f, 250f)))
    }

    @Test
    fun aPressInTheSameDialogAnswers() {
        rule.lift(DIALOG, 1, LIFTED_AT, POINT)

        assertFalse(rule.press(DIALOG, 2, LIFTED_AT + 100, POINT, TIMEOUT, DISTANCE))
    }

    @Test
    fun aPressInAnOlderDialogIsIgnored() {
        rule.lift(DIALOG, 1, LIFTED_AT, POINT)

        assertTrue(rule.press(OLDER_DIALOG, 2, LIFTED_AT + 100, POINT, TIMEOUT, DISTANCE))
    }

    @Test
    fun anIgnoredPressStaysIgnoredUntilItLifts() {
        rule.lift(DIALOG, 1, LIFTED_AT, POINT)
        rule.mainPress(2, LIFTED_AT + 100)
        assertTrue(rule.isIgnored(2))

        rule.lift(null, 2, LIFTED_AT + 5_000, POINT)

        assertFalse(rule.isIgnored(2))
    }

    @Test
    fun aBurstOfThreeStaysIgnoredAndAPauseEndsIt() {
        rule.lift(DIALOG, 1, LIFTED_AT, POINT)
        assertTrue(rule.mainPress(2, LIFTED_AT + 150))
        rule.lift(null, 2, LIFTED_AT + 190, POINT)

        assertTrue("the third, measured from the second's lift", rule.mainPress(3, LIFTED_AT + 450))
        rule.lift(null, 3, LIFTED_AT + 490, POINT)

        assertFalse("a pause", rule.mainPress(4, LIFTED_AT + 790))
    }

    @Test
    fun anIgnoredLiftMovesThePlaceToo() {
        rule.lift(DIALOG, 1, LIFTED_AT, POINT)
        val moved = POINT + Offset(0f, 200f)
        rule.mainPress(2, LIFTED_AT + 100, moved)
        rule.lift(null, 2, LIFTED_AT + 140, moved)

        assertTrue(rule.mainPress(3, LIFTED_AT + 200, moved + Offset(0f, 200f)))
    }

    @Test
    fun anIgnoredLiftStaysTheDialogs() {
        rule.lift(DIALOG, 1, LIFTED_AT, POINT)
        rule.press(OLDER_DIALOG, 2, LIFTED_AT + 100, POINT, TIMEOUT, DISTANCE)
        rule.lift(OLDER_DIALOG, 2, LIFTED_AT + 140, POINT)

        assertTrue(
            "still the upper dialog's, so the older one's next press is ignored",
            rule.press(OLDER_DIALOG, 3, LIFTED_AT + 200, POINT, TIMEOUT, DISTANCE)
        )
        assertFalse(
            "and the upper dialog's own press is not",
            rule.press(DIALOG, 4, LIFTED_AT + 200, POINT, TIMEOUT, DISTANCE)
        )
    }

    @Test
    fun aStaleLiftIsNotRevivedByALaterLiftOfAnotherPointer() {
        rule.lift(DIALOG, 1, LIFTED_AT, POINT)
        assertFalse("beyond the timeout", rule.mainPress(2, LIFTED_AT + 1_000))
        rule.lift(null, 2, LIFTED_AT + 1_040, POINT)

        assertFalse(rule.mainPress(3, LIFTED_AT + 1_100))
    }

    @Test
    fun aMainWindowPressThatAnsweredDoesNotMoveTheLift() {
        rule.lift(DIALOG, 1, LIFTED_AT, POINT)
        val far = POINT + Offset(0f, 1_000f)
        assertFalse(rule.mainPress(2, LIFTED_AT + 50, far))
        rule.lift(null, 2, LIFTED_AT + 90, far)

        assertFalse("not within the distance of that lift", rule.mainPress(3, LIFTED_AT + 100, far))
        assertTrue("still within this one", rule.mainPress(4, LIFTED_AT + 100))
    }

    @Test
    fun twoPointersAreJudgedApart() {
        rule.lift(DIALOG, 1, LIFTED_AT, POINT)
        assertTrue(rule.mainPress(2, LIFTED_AT + 100))
        assertFalse(rule.mainPress(3, LIFTED_AT + 100, POINT + Offset(0f, 1_000f)))

        assertTrue(rule.isIgnored(2))
        assertFalse(rule.isIgnored(3))
    }

    @Test
    fun aPointerIdUsedAgainDecidesAfresh() {
        rule.lift(DIALOG, 1, LIFTED_AT, POINT)
        rule.mainPress(2, LIFTED_AT + 100)

        assertFalse(rule.mainPress(2, LIFTED_AT + 5_000))
        assertFalse(rule.isIgnored(2))
    }
}
