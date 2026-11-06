package com.migrely.app.designsystem.input

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val CAP = 5

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LimitedFieldTest {
    @get:Rule
    val rule = createComposeRule()

    private var stored by mutableStateOf("")
    private var key by mutableStateOf("first")
    private val heard = mutableListOf<String>()

    private val node get() = rule.onNode(hasSetTextAction())

    @Composable
    private fun Field() {
        val field = rememberLimitedField(stored, CAP, key) {
            heard += it
            stored = it
        }
        BasicTextField(value = field.value, onValueChange = field.onValueChange)
    }

    private fun show(text: String) {
        stored = text
        rule.setContent { Field() }
        rule.waitForIdle()
    }

    @Test
    fun whenShownThenTheCursorIsAtTheEnd() {
        show("abc")

        assertEquals("abc", node.text())
        assertEquals(TextRange(3), node.selection())
    }

    @Test
    fun whenTypingThenTheTextIsHeard() {
        show("abc")

        node.performTextInput("d")

        assertEquals(listOf("abcd"), heard)
        assertEquals("abcd", node.text())
    }

    @Test
    fun whenMovingTheCursorThenNothingIsHeardAndTheCursorStays() {
        show("abc")

        node.performTextInputSelection(TextRange(1))

        assertEquals(emptyList<String>(), heard)
        assertEquals(TextRange(1), node.selection())
    }

    @Test
    fun givenAFullFieldWhenAnEditIsRefusedThenNothingIsHeardAndTheCursorStays() {
        show("abcde")
        node.performTextInputSelection(TextRange(2))

        node.performTextInput("Q")

        assertEquals(emptyList<String>(), heard)
        assertEquals("abcde", node.text())
        assertEquals(TextRange(2), node.selection())
    }

    @Test
    fun givenOneFrameWhenASecondEditBringsBackTheTextThenShownIsStored() {
        show("abc")
        node.performTextInputSelection(TextRange(1))
        val setText = node.setText()

        rule.runOnUiThread {
            setText("ab")
            setText("abc")
        }
        rule.waitForIdle()

        assertEquals(stored, node.text())
        assertEquals(TextRange(stored.length), node.selection())
    }

    @Test
    fun whenTheStoredTextChangesElsewhereThenItIsShownWithTheCursorAtTheEnd() {
        show("abc")
        node.performTextInputSelection(TextRange(1))

        stored = "merged"
        rule.waitForIdle()

        assertEquals("merged", node.text())
        assertEquals(TextRange(6), node.selection())
        assertEquals(emptyList<String>(), heard)
    }

    @Test
    fun givenAStoredTextOverwrittenInTheSameFrameWhenTypingTheSameLetterThenItIsHeard() {
        show("abc")
        node.performTextInputSelection(TextRange(3))
        val setText = node.setText()
        rule.runOnUiThread {
            setText("abcd")
            stored = "abc"
        }
        rule.waitForIdle()
        assertEquals("abc", node.text())

        node.performTextInput("d")

        assertEquals("abcd", stored)
        assertEquals("abcd", node.text())
    }

    @Test
    fun whenAKeyChangesThenTheCursorIsForgotten() {
        show("abc")
        node.performTextInputSelection(TextRange(1))

        key = "second"
        rule.waitForIdle()

        assertEquals(TextRange(3), node.selection())
    }

    @Test
    fun whenRecreatedThenTheCursorIsRestored() {
        stored = "abc"
        val restoration = StateRestorationTester(rule)
        restoration.setContent { Field() }
        node.performTextInputSelection(TextRange(1))

        restoration.emulateSavedInstanceStateRestore()

        assertEquals("abc", node.text())
        assertEquals(TextRange(1), node.selection())
    }
}

private fun SemanticsNodeInteraction.setText(): (String) -> Boolean {
    val action = fetchSemanticsNode().config[SemanticsActions.SetText].action!!
    return { text -> action(AnnotatedString(text)) }
}

private fun SemanticsNodeInteraction.text(): String =
    fetchSemanticsNode().config[SemanticsProperties.EditableText].text

private fun SemanticsNodeInteraction.selection(): TextRange =
    fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange]
