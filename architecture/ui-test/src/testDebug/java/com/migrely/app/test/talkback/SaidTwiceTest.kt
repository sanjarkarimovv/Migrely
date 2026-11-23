package com.migrely.app.test.talkback

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * A switch row with a [title] and a [summary]; TalkBack says the state ("On", or "Off" when not [checked]), the title
 * and the summary.
 */
private fun switchRow(
    title: String,
    summary: String,
    checked: Boolean = true
): @Composable () -> Unit = {
    Column(Modifier.toggleable(value = checked, role = Role.Switch, onValueChange = {})) {
        BasicText(title)
        BasicText(summary)
    }
}

/**
 * The walk's "said twice" check compares the control's words, whole and in any case (BRD D181 (2)): a part repeated as
 * words fails, also as a shorter form of 4 letters or more ("note" in "notes"); a shorter word that only starts another
 * ("On" in "One", "Off" in "Offline") is not a repeat. The state ("On") counts only when another part is exactly the
 * same word, as a switch titled "On"; next to the word "on" in a summary it is no repeat. Red first: the state test on
 * the substring check and again on the case-blind check, the case and short-form tests on the exact-word check, the
 * title-equals-state test on the check that left the state out (code review of 19b2e83d).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35])
class SaidTwiceTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun saidTwice(screens: Map<String, @Composable () -> Unit>) =
        rule.talkBackWalk(1f, screens).filter { "said twice" in it }

    @Test
    fun `Given the same words as title and summary then said twice, also inside a longer text`() =
        assertEquals(
            listOf(
                "Same: said twice: On | Include notes | Include notes",
                "Inside: said twice: On | Notes | Your Notes on each migraine"
            ),
            saidTwice(
                mapOf(
                    "Same" to switchRow("Include notes", "Include notes"),
                    "Inside" to switchRow("Notes", "Your Notes on each migraine")
                )
            )
        )

    @Test
    fun `Given the state's word in the summary then not said twice, it is not the control's`() {
        val row = switchRow("Mention Migrely", "One small line on the last page")
        assertEquals(emptyList<String>(), saidTwice(mapOf("State" to row)))
    }

    @Test
    fun `Given the same words in another case or a shorter form then said twice`() = assertEquals(
        listOf(
            "Case: said twice: On | Include notes | include notes on each migraine",
            "Form: said twice: On | Include note | Include notes on each migraine"
        ),
        saidTwice(
            mapOf(
                "Case" to switchRow("Include notes", "include notes on each migraine"),
                "Form" to switchRow("Include note", "Include notes on each migraine")
            )
        )
    )

    @Test
    fun `Given the same words with another apostrophe then said twice`() = assertEquals(
        listOf("Apostrophe: said twice: On | Couldn't save that | Couldn\u2019t save that"),
        saidTwice(
            mapOf("Apostrophe" to switchRow("Couldn't save that", "Couldn\u2019t save that"))
        )
    )

    @Test
    fun `Given a title that is exactly the state then said twice`() = assertEquals(
        listOf(
            "On: said twice: On | On | One small line",
            "Off: said twice: Off | Off | Offline backup"
        ),
        saidTwice(
            mapOf(
                "On" to switchRow("On", "One small line"),
                "Off" to switchRow("Off", "Offline backup", checked = false)
            )
        )
    )

    @Test
    fun `Given a short word that only starts another then not said twice`() = assertEquals(
        emptyList<String>(),
        saidTwice(
            mapOf(
                "On" to switchRow("On", "One small line", checked = false),
                "Off" to switchRow("Off", "Offline backup"),
                "Clean" to switchRow("Mention Migrely", "One small line on the last page")
            )
        )
    )
}
