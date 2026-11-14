package com.migrely.app.attack.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
import com.migrely.app.designsystem.snackbar.MigrelySnackbar
import com.migrely.app.designsystem.snackbar.MigrelySnackbarHost
import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val DISMISS = "Dismiss"

/**
 * Attack details with the safety card (BRD D178 (4), EC-UX-36, ux O16, O17; #185): Back, then the message's Dismiss,
 * then "Got it", then the content.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h700dp")
class AttackDetailsMessageOrderTest {
    @get:Rule
    val rule = createComposeRule()

    private lateinit var focusManager: FocusManager

    @Composable
    private fun TestMessage() {
        MigrelySnackbarHost(message = "Couldn't save.", reduceMotion = true) {
            MigrelySnackbar(it, SnackbarTone.Error, null, null, DISMISS, {})
        }
    }

    private fun tab() {
        rule.runOnIdle { focusManager.moveFocus(FocusDirection.Next) }
        rule.waitForIdle()
    }

    @Test
    fun `Tab goes through Back, then the message, then Got it on the safety card`() {
        rule.setContent {
            focusManager = LocalFocusManager.current
            MigrelyTheme {
                AttackDetailsContents(
                    viewState = AttackDetailsViewStateUiModel.Ready(previewDetails()),
                    timeUiFormatter = previewTimeFormatter,
                    labels = AttackLabels.Default,
                    localState = AttackDetailsLocalState(
                        timeError = null,
                        safetyCardVisible = true,
                        onDismissSafetyCard = {},
                        onTimeErrorShown = {}
                    ),
                    actions = previewActions,
                    messages = { TestMessage() }
                )
            }
        }

        tab()
        rule.onNodeWithContentDescription("Back").assertIsFocused()
        tab()
        rule.onNodeWithContentDescription(DISMISS).assertIsFocused()
        tab()
        rule.onNodeWithText("Got it").assertIsFocused()
    }
}
