package com.migrely.app.attack.ui.view

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The custom tag dialog's width (T23 8.1 row R03, cr-de round 2 F6 (a)): wide in a short window, unless narrow for the
 * keyboard. The decision is tested apart from the dialog: a text field in a dialog runs Robolectric out of memory.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w640dp-h360dp-land")
class CustomTagDialogWidthTest {
    @get:Rule
    val rule = createComposeRule()

    private fun wide(widthRule: KeyboardWidthRule, hardwareKeyboard: Boolean = false): Boolean {
        var wide: Boolean? = null
        rule.setContent {
            WithHardwareKeyboard(hardwareKeyboard) { wide = customTagDialogWide(widthRule) }
        }
        rule.waitForIdle()
        return checkNotNull(wide)
    }

    @Test
    fun `Given a short window at rest then the dialog is wide`() {
        assertTrue(wide(KeyboardWidthRule()))
    }

    @Test
    fun `Given a short window while it is narrow for the keyboard then the dialog is not wide`() {
        val widthRule = KeyboardWidthRule().apply { onTypingIntent() }

        assertFalse(wide(widthRule))
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp-port")
    fun `Given a tall window then the dialog is not wide`() {
        assertFalse(wide(KeyboardWidthRule()))
    }

    /** Spec custom-tags, "A hardware keyboard is attached": no on-screen keyboard comes, so it never narrows. */
    @Test
    fun `Given a hardware keyboard when the person means to type then the dialog stays wide`() {
        val widthRule = KeyboardWidthRule().apply { onTypingIntent() }

        assertTrue(wide(widthRule, hardwareKeyboard = true))
    }
}
