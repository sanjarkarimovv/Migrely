package com.migrely.app.attack.ui.view

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The custom tag dialog is narrow only for the keyboard (T23 8.1 row R03, ux F3): from an intent to type until the
 * keyboard closes, or until the wait for a keyboard ends with none shown (cr-de round 2, F1 and F2).
 */
class KeyboardWidthRuleTest {
    private val classUnderTest = KeyboardWidthRule()

    @Test
    fun `Given the dialog at rest then it is not narrow`() {
        assertFalse(classUnderTest.narrow)
    }

    @Test
    fun `When the person means to type then it is narrow before the keyboard rises`() {
        classUnderTest.onTypingIntent()

        assertTrue(classUnderTest.narrow)
    }

    /** F1: a hardware keyboard, or no IME at all. */
    @Test
    fun `Given no keyboard rose when the wait ends then it is wide again`() {
        classUnderTest.onTypingIntent()

        classUnderTest.onKeyboardWaitOver()

        assertFalse(classUnderTest.narrow)
    }

    @Test
    fun `Given the keyboard rose when the wait ends then it stays narrow`() {
        classUnderTest.onTypingIntent()
        classUnderTest.onKeyboard(true)

        classUnderTest.onKeyboardWaitOver()

        assertTrue(classUnderTest.narrow)
    }

    @Test
    fun `When the keyboard closes while the field keeps focus then it is wide again`() {
        classUnderTest.onTypingIntent()
        classUnderTest.onKeyboard(true)

        classUnderTest.onKeyboard(false)

        assertFalse(classUnderTest.narrow)
    }

    /** F2: Back closed the keyboard and the field kept focus; a second tap raises it again. */
    @Test
    fun `Given the keyboard closed when the field is tapped again then it is narrow at once`() {
        classUnderTest.onTypingIntent()
        classUnderTest.onKeyboard(true)
        classUnderTest.onKeyboard(false)

        classUnderTest.onTypingIntent()

        assertTrue(classUnderTest.narrow)
    }

    @Test
    fun `When the keyboard rises with no intent seen then it is narrow`() {
        classUnderTest.onKeyboard(true)

        assertTrue(classUnderTest.narrow)
    }

    @Test
    fun `When the person means to type again then the wait starts again`() {
        classUnderTest.onTypingIntent()

        classUnderTest.onTypingIntent()

        assertEquals(2, classUnderTest.typingIntents)
    }
}
