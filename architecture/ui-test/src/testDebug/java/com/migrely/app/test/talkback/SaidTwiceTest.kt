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

/** A switch row with a [title] and a [summary]; TalkBack says the state ("On"), the title and the summary. */
private fun switchRow(title: String, summary: String): @Composable () -> Unit = {
    Column(Modifier.toggleable(value = true, role = Role.Switch, onValueChange = {})) {
        BasicText(title)
        BasicText(summary)
    }
}

/**
 * The walk's "said twice" check compares whole words (BRD D181 (2)): a part repeated as words fails, a part that is
 * only the start of another word (the state "On" in "One") does not. The second test failed on the substring check.
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
    fun `Given the state only starts a word of the text then not said twice`() {
        val row = switchRow("Mention Migrely", "One small line on the last page")
        assertEquals(emptyList<String>(), saidTwice(mapOf("Start" to row)))
    }
}
