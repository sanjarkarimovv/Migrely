package com.migrely.app.designsystem.dialog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

private const val APPEARED = 10_000L
private const val FINGER = 1L
private const val OTHER_FINGER = 2L

@RunWith(Parameterized::class)
class DialogWaitRuleGapTest(private val gap: Long, private val ignored: Boolean) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0} ms after it appeared, ignored: {1}")
        fun parameters() = listOf(
            arrayOf<Any>(0L, true),
            arrayOf<Any>(100L, true),
            arrayOf<Any>(499L, true),
            arrayOf<Any>(500L, false),
            arrayOf<Any>(501L, false),
            arrayOf<Any>(800L, false)
        )
    }

    @Test
    fun `Given a dialog that appeared when a press comes at this gap then the rule says`() {
        assertEquals(ignored, DialogWaitRule().press(FINGER, APPEARED + gap, APPEARED))
    }
}

class DialogWaitRuleTest {
    private val rule = DialogWaitRule()

    @Test
    fun `Given a press made before the dialog appeared when it arrives then it is ignored`() {
        assertTrue(rule.press(FINGER, APPEARED - 50, APPEARED))
    }

    @Test
    fun `Given a press inside the wait when asked after the end then it stays ignored`() {
        rule.press(FINGER, APPEARED + 400, APPEARED)

        assertTrue(rule.isIgnored(FINGER))
    }

    @Test
    fun `Given a held press when it lifts then it is no longer ignored`() {
        rule.press(FINGER, APPEARED + 400, APPEARED)

        rule.lift(FINGER)

        assertFalse(rule.isIgnored(FINGER))
    }

    @Test
    fun `Given a press after the wait when asked then it is not ignored`() {
        rule.press(FINGER, APPEARED + 500, APPEARED)

        assertFalse(rule.isIgnored(FINGER))
    }

    @Test
    fun `Given two pointers when one began in the wait then each is judged by itself`() {
        rule.press(FINGER, APPEARED + 400, APPEARED)
        rule.press(OTHER_FINGER, APPEARED + 600, APPEARED)

        assertTrue(rule.isIgnored(FINGER))
        assertFalse(rule.isIgnored(OTHER_FINGER))
    }

    @Test
    fun `Given the next press after a held one lifted when it is past the wait then it answers`() {
        rule.press(FINGER, APPEARED + 400, APPEARED)
        rule.lift(FINGER)

        assertFalse(rule.press(FINGER, APPEARED + 900, APPEARED))
        assertFalse(rule.isIgnored(FINGER))
    }
}
