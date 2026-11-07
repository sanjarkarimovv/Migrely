package com.migrely.app.designsystem.dialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performTouchInput
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val FRAMES_TO_APPEAR = 20
private const val IN_THE_WAIT = 300L

/**
 * On a device, because a text field in a dialog runs the JVM tests out of memory on Robolectric. The
 * gaps are measured from the moment the dialog's own clock read.
 */
class DialogWaitTextFieldTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var appearedAtMillis: Long? = null
    private var showing by mutableStateOf(false)

    private fun show() {
        // As for a person, whose tap opened it. Out of touch mode Android focuses the field.
        InstrumentationRegistry.getInstrumentation().setInTouchMode(true)
        composeRule.setContent {
            CompositionLocalProvider(
                LocalDialogClock provides {
                    composeRule.mainClock.currentTime.also { appearedAtMillis = it }
                }
            ) {
                MaterialTheme { if (showing) TheDialog() }
            }
        }
        composeRule.waitForIdle()
        composeRule.mainClock.autoAdvance = false
        composeRule.runOnUiThread { showing = true }
        var frames = 0
        while (!isUp() && frames++ < FRAMES_TO_APPEAR) {
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.waitForIdle()
        }
        assertTrue("the dialog did not appear", isUp())
        if (appearedAtMillis == null) appearedAtMillis = composeRule.mainClock.currentTime
    }

    private fun isUp() = composeRule.onAllNodes(isDialog(), useUnmergedTree = true)
        .fetchSemanticsNodes().isNotEmpty()

    @Composable
    private fun TheDialog() {
        MigrelyAlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            text = {
                var text by remember { mutableStateOf("") }
                OutlinedTextField(value = text, onValueChange = { text = it })
            }
        )
    }

    private fun goTo(millis: Long) {
        val left = checkNotNull(appearedAtMillis) + millis - composeRule.mainClock.currentTime
        composeRule.mainClock.advanceTimeBy(left, ignoreFrameDuration = true)
        composeRule.waitForIdle()
    }

    @Test
    fun aShortTapOnATextFieldDoesNotFocusAndTheNextDoes() {
        show()
        val field = composeRule.onNode(hasSetTextAction(), useUnmergedTree = true)
        goTo(IN_THE_WAIT)
        field.performTouchInput {
            down(center)
            up()
        }
        composeRule.waitForIdle()
        field.assertIsNotFocused()

        goTo(DIALOG_WAIT_MILLIS)
        field.performTouchInput {
            down(center)
            up()
        }
        composeRule.waitForIdle()

        field.assertIsFocused()
    }
}
