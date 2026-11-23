package com.migrely.app.attack.ui.view

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The custom tag dialog is narrow only for the keyboard (T23 8.1 row R03, ux F3). */
class KeyboardWidthRuleTest {
    private val classUnderTest = KeyboardWidthRule()

    @Test
    fun `Given the dialog at rest then it is not narrow`() {
        assertFalse(classUnderTest.narrow)
    }

    @Test
    fun `When the field takes focus then it is narrow before the keyboard rises`() {
        classUnderTest.onFieldFocus(true)

        assertTrue(classUnderTest.narrow)
    }

    @Test
    fun `When the keyboard closes while the field keeps focus then it is wide again`() {
        classUnderTest.onFieldFocus(true)
        classUnderTest.onKeyboard(true)

        classUnderTest.onKeyboard(false)

        assertFalse(classUnderTest.narrow)
    }

    @Test
    fun `When the keyboard rises again then it is narrow again`() {
        classUnderTest.onFieldFocus(true)
        classUnderTest.onKeyboard(false)

        classUnderTest.onKeyboard(true)

        assertTrue(classUnderTest.narrow)
    }
}
