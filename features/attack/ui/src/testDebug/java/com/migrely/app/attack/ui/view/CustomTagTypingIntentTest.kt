package com.migrely.app.attack.ui.view

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Every tap on the custom tag field is an intent to type (cr-de round 2, F2): after Back closed the keyboard the field
 * keeps focus, and the next tap raises the keyboard again, so the dialog must narrow then too, not only on focus.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class, qualifiers = "w640dp-h326dp-land-xxhdpi")
class CustomTagTypingIntentTest {
    @get:Rule
    val rule = createComposeRule()

    private var intents = 0

    // At 200% in the squeezed area the dialog had on the device (R03), so a drag on the field scrolls the area.
    private fun show() {
        RuntimeEnvironment.setFontScale(2f)
        rule.setContent {
            MigrelyTheme {
                Box(Modifier.size(272.dp, 138.dp).clipToBounds()) {
                    CustomTagNameArea(
                        title = "Your own symptom",
                        name = TextFieldValue(),
                        onTypingIntent = { intents++ }
                    ) {}
                }
            }
        }
    }

    /** The field focused by a first tap, as before a paste or a scroll; returns the intents so far. */
    private fun focusedByATap(): Int {
        show()
        rule.onNode(hasSetTextAction()).performClick()
        return intents
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

    /** A drag to scroll ends the press cancelled: not an intent (spec custom-tags, "A drag on the field to scroll"). */
    @Test
    fun `When a drag on the focused field scrolls the area then it is not an intent to type`() {
        val before = focusedByATap()

        rule.onNode(hasSetTextAction()).performTouchInput { swipeUp() }

        assertEquals("intents after the drag", before, intents)
    }

    /** A long press to paste is not an intent (spec custom-tags, "A long press to paste"). */
    @Test
    fun `When the focused field is long-pressed then it is not an intent to type`() {
        val before = focusedByATap()

        rule.onNode(hasSetTextAction()).performTouchInput { longClick() }

        assertEquals("intents after the long press", before, intents)
    }
}
