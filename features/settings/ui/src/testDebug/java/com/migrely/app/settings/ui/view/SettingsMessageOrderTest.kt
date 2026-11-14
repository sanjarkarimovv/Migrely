package com.migrely.app.settings.ui.view

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import com.migrely.app.designsystem.snackbar.MigrelySnackbar
import com.migrely.app.designsystem.snackbar.MigrelySnackbarHost
import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.settings.ui.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val DISMISS = "Dismiss"

/** Settings: Back, then the message's Dismiss, then the settings (BRD D178 (4), EC-UX-36, ux O1). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class SettingsMessageOrderTest {
    @get:Rule
    val rule = createComposeRule()

    private lateinit var focusManager: FocusManager

    private var back = ""

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
    fun `Tab goes through Back, then the message, then the settings`() {
        rule.setContent {
            focusManager = LocalFocusManager.current
            back = stringResource(R.string.settings_back)
            MigrelyTheme {
                SettingsContents(
                    settings = PREVIEW_SETTINGS,
                    actions = SettingsActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {}),
                    messages = { TestMessage() }
                )
            }
        }
        tab()
        rule.onNodeWithContentDescription(back).assertIsFocused()
        tab()
        rule.onNodeWithContentDescription(DISMISS).assertIsFocused()

        // Then the content: something else takes focus.
        tab()
        rule.onNodeWithContentDescription(DISMISS).assertIsNotFocused()
        rule.onNode(isFocused()).assertExists()
    }
}
