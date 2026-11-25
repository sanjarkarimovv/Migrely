package com.migrely.app.attack.ui.view

import android.app.Application
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * With no on-screen keyboard up 3000 ms after the latest intent to type, the custom tag dialog is wide again (spec
 * custom-tags, "No on-screen keyboard comes"; design D10 item 6). The test clock drives the wait.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class KeyboardWaitTest {
    @get:Rule
    val rule = createComposeRule()

    private val widthRule = KeyboardWidthRule()

    @Before
    fun setUp() {
        rule.mainClock.autoAdvance = false
        rule.setContent { KeyboardWait(widthRule) }
    }

    private fun onUi(action: () -> Unit) {
        rule.runOnUiThread {
            action()
            Snapshot.sendApplyNotifications()
        }
        rule.mainClock.advanceTimeByFrame()
    }

    @Test
    fun `Given no keyboard then the dialog stays narrow until 3000 ms after the intent`() {
        onUi { widthRule.onTypingIntent() }

        rule.mainClock.advanceTimeBy(BEFORE_THE_WAIT_ENDS)

        assertTrue(widthRule.narrow)
    }

    @Test
    fun `Given no keyboard when 3000 ms have passed then the dialog is wide again`() {
        onUi { widthRule.onTypingIntent() }

        rule.mainClock.advanceTimeBy(AFTER_THE_WAIT_ENDS)

        assertFalse(widthRule.narrow)
    }

    @Test
    fun `Given a later intent then the wait starts again from it`() {
        onUi { widthRule.onTypingIntent() }
        rule.mainClock.advanceTimeBy(2_000)
        onUi { widthRule.onTypingIntent() }

        rule.mainClock.advanceTimeBy(BEFORE_THE_WAIT_ENDS)

        assertTrue(widthRule.narrow)
    }

    @Test
    fun `Given the keyboard rose when the wait ends then the dialog stays narrow`() {
        onUi { widthRule.onTypingIntent() }
        onUi { widthRule.onKeyboard(true) }

        rule.mainClock.advanceTimeBy(AFTER_THE_WAIT_ENDS)

        assertTrue(widthRule.narrow)
    }

    private companion object {
        const val BEFORE_THE_WAIT_ENDS = 2_990L
        const val AFTER_THE_WAIT_ENDS = 3_090L
    }
}
