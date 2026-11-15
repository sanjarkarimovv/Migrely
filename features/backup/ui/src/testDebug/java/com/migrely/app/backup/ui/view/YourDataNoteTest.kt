package com.migrely.app.backup.ui.view

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Legal's final wording (BRD D181 (1), `reports/2026-11-15-legal-t66-wording.md`), written here
 * and not read from `R.string`, so a wording drift fails.
 */
private const val LEGAL_NOTE = "Migrely has no internet permission. Your entries stay on this " +
    "phone, in Android's own backup if it's on, in a transfer to a new phone, and in any file " +
    "you export."

/** Your data's note names every way entries leave the phone (BRD D181 (1), EC-STO-11). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w360dp-h640dp")
class YourDataNoteTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun `When shown then the note reads legal's text`() {
        // When
        rule.setContent {
            MigrelyTheme {
                YourDataContents(
                    working = false,
                    automaticCopy = null,
                    actions = YourDataActions({}, {}, {}, {}, {}),
                    messages = {}
                )
            }
        }

        // Then
        rule.onNodeWithText(LEGAL_NOTE).performScrollTo().assertExists()
    }
}
