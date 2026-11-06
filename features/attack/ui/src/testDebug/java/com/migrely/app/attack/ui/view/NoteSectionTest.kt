package com.migrely.app.attack.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.text.TextRange
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val NOTE = "note of attack A"

/** The note on rememberLimitedField: it opens at its end and is bound to its attack (#91). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h1200dp")
class NoteSectionTest {
    @get:Rule
    val rule = createComposeRule()

    private var attack by mutableStateOf("A")
    private var saved by mutableStateOf(NOTE)
    private val writes = mutableListOf<Pair<String, String>>()

    private val node get() = rule.onNode(hasSetTextAction())

    @Composable
    private fun Screen() = MigrelyTheme {
        var draft by rememberSaveable(attack) { mutableStateOf<String?>(null) }
        val id = attack
        NoteSection(
            attackId = id,
            draft = draft,
            savedNote = saved,
            onDraftChange = { draft = it },
            onNote = { writes += id to it }
        )
    }

    @Test
    fun whenOpenedThenTheCursorIsAtTheEnd() {
        rule.setContent { Screen() }

        assertEquals(NOTE, node.text())
        assertEquals(TextRange(NOTE.length), node.selection())
    }

    @Test
    fun givenAnotherAttackWithOtherTextThenItsTextShowsWithTheCursorAtTheEnd() {
        rule.setContent { Screen() }
        node.performTextInputSelection(TextRange(2))

        rule.runOnIdle {
            attack = "B"
            saved = "B text"
        }
        rule.waitForIdle()

        assertEquals("B text", node.text())
        assertEquals(TextRange("B text".length), node.selection())
        assertEquals(emptyList<Pair<String, String>>(), writes)
    }

    @Test
    fun givenAnotherAttackWithTheSameTextThenTheCursorIsForgotten() {
        rule.setContent { Screen() }
        node.performTextInputSelection(TextRange(2))

        rule.runOnIdle { attack = "B" }
        rule.waitForIdle()

        assertEquals(TextRange(NOTE.length), node.selection())
    }

    @Test
    fun givenATypedDraftWhenTheAttackChangesThenTheOtherTextShows() {
        rule.setContent { Screen() }
        node.performTextInput(" more")

        rule.runOnIdle {
            attack = "B"
            saved = "B text"
        }
        rule.waitForIdle()

        assertEquals("B text", node.text())
    }

    @Test
    fun givenATypedDraftWhenTheDelayPassesThenItIsSaved() {
        rule.setContent { Screen() }
        node.performTextInput(" more")

        rule.mainClock.advanceTimeBy(5_000)
        rule.waitForIdle()

        assertEquals(listOf("A" to "$NOTE more"), writes)
    }

    @Test
    fun whenRecreatedWithTheSameTextThenTheCursorIsRestored() {
        val restoration = StateRestorationTester(rule)
        restoration.setContent { Screen() }
        node.performTextInputSelection(TextRange(2))

        restoration.emulateSavedInstanceStateRestore()
        rule.waitForIdle()

        assertEquals(TextRange(2), node.selection())
    }

    @Test
    fun whenRecreatedWithChangedTextThenItShowsWithTheCursorAtTheEnd() {
        val restoration = StateRestorationTester(rule)
        restoration.setContent { Screen() }
        node.performTextInputSelection(TextRange(2))

        rule.runOnIdle { saved = "changed while away" }
        restoration.emulateSavedInstanceStateRestore()
        rule.waitForIdle()

        assertEquals("changed while away", node.text())
        assertEquals(TextRange("changed while away".length), node.selection())
    }

    @Test
    fun whenAMergeDropsTheDraftThenTheMergedNoteShowsWithTheCursorAtTheEnd() {
        var dropDraft: () -> Unit = {}
        rule.setContent {
            MigrelyTheme {
                var draft by rememberSaveable { mutableStateOf<String?>(null) }
                dropDraft = { draft = null }
                NoteSection("A", draft, saved, { draft = it }, { writes += "A" to it })
            }
        }
        node.performTextInput(" typed")
        node.performTextInputSelection(TextRange(3))

        rule.runOnIdle {
            dropDraft()
            saved = "merged\n\nnote"
        }
        rule.waitForIdle()

        assertEquals("merged\n\nnote", node.text())
        assertEquals(TextRange("merged\n\nnote".length), node.selection())
    }
}

private fun SemanticsNodeInteraction.text(): String =
    fetchSemanticsNode().config[SemanticsProperties.EditableText].text

private fun SemanticsNodeInteraction.selection(): TextRange =
    fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange]
