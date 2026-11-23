package com.migrely.app.attack.ui.view

import android.app.Application
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.input.TextFieldValue
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Every tap on the custom tag field is an intent to type (cr-de round 2, F2): after Back closed the keyboard the field
 * keeps focus, and the next tap raises the keyboard again, so the dialog must narrow then too, not only on focus.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w640dp-h360dp-land")
class CustomTagTypingIntentTest {
    @get:Rule
    val rule = createComposeRule()

    private var intents = 0

    private fun show() = rule.setContent {
        MigrelyTheme {
            CustomTagNameArea(title = "Your own symptom", name = TextFieldValue(), onTypingIntent = {
                intents++
            }) {}
        }
    }

    @Test
    fun `When the field is tapped then it is an intent to type`() {
        show()

        rule.onNode(hasSetTextAction()).performClick()

        assertTrue("no intent on the first tap", intents >= 1)
    }

    @Test
    fun `When the focused field is tapped again then it is another intent to type`() {
        show()
        rule.onNode(hasSetTextAction()).performClick()
        val afterFirstTap = intents

        rule.onNode(hasSetTextAction()).performClick()

        assertTrue(
            "no intent on the second tap ($afterFirstTap before, $intents after)",
            intents > afterFirstTap
        )
    }
}
