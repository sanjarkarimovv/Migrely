package com.migrely.app.designsystem.dialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performTouchInput
import org.junit.Rule
import org.junit.Test

private const val IN_THE_WAIT = 300L

/**
 * On a device, because a text field in a dialog runs the JVM tests out of memory on Robolectric.
 */
class DialogWaitTextFieldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun aShortTapOnATextFieldDoesNotFocusAndTheNextDoes() {
        var appearedAtMillis = 0L
        composeRule.setContent {
            CompositionLocalProvider(
                LocalDialogClock provides {
                    composeRule.mainClock.currentTime.also { appearedAtMillis = it }
                }
            ) {
                MaterialTheme {
                    MigrelyAlertDialog(
                        onDismissRequest = {},
                        confirmButton = {},
                        text = {
                            var text by remember { mutableStateOf("") }
                            OutlinedTextField(value = text, onValueChange = { text = it })
                        }
                    )
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.mainClock.autoAdvance = false
        val field = composeRule.onNode(hasSetTextAction(), useUnmergedTree = true)

        val pressedAtMillis = composeRule.mainClock.currentTime
        check(pressedAtMillis - appearedAtMillis < DIALOG_WAIT_MILLIS - IN_THE_WAIT) {
            "the dialog took ${pressedAtMillis - appearedAtMillis} ms to settle"
        }
        field.performTouchInput {
            down(center)
            up()
        }
        composeRule.waitForIdle()
        field.assertIsNotFocused()

        composeRule.mainClock.advanceTimeBy(DIALOG_WAIT_MILLIS, ignoreFrameDuration = true)
        field.performTouchInput {
            down(center)
            up()
        }
        composeRule.waitForIdle()

        field.assertIsFocused()
    }
}
