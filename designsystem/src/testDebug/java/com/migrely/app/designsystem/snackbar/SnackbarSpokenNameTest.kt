package com.migrely.app.designsystem.snackbar

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val MESSAGE = "Migraine started"

/** The message's action control reads its description once (BRD D181 (2), EC-UX-45). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h800dp")
class SnackbarSpokenNameTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `The action control reads its description once, and the message stays readable`() {
        // When
        rule.setContent {
            MigrelyTheme {
                MigrelySnackbar(
                    text = MESSAGE,
                    tone = SnackbarTone.Information,
                    action = SnackbarAction("Undo", "Undo the start") {},
                    baseDurationMillis = null,
                    dismissLabel = "Dismiss",
                    onDismiss = {}
                )
            }
        }

        // Then
        rule.assertOneSpokenName(spoken = "Undo the start", visible = "Undo")
        rule.onNodeWithText(MESSAGE).assertExists()
    }
}
