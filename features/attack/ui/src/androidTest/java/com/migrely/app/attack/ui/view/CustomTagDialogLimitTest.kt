package com.migrely.app.attack.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.text.TextRange
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.attack.ui.model.TagKindUiModel
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val CAP = 40
private const val THUMBS_UP = "👍"

/** The custom tag name keeps its end, its cursor and whole characters at the cap (#91). */
@RunWith(AndroidJUnit4::class)
class CustomTagDialogLimitTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private var added: String? = null

    private val field get() = composeTestRule.onNode(hasSetTextAction())

    private fun showDialog() = composeTestRule.setContent { Dialog() }

    @Composable
    private fun Dialog() = MigrelyTheme {
        CustomTagDialog(
            kind = TagKindUiModel.Symptom,
            onAdd = { added = it },
            onDismiss = {}
        )
    }

    @Test
    fun givenFullNameWhenTypingInTheMiddleThenEndAndCursorStay() {
        showDialog()
        val full = "a".repeat(CAP - 1) + "z"
        field.performTextInput(full)

        field.performTextInputSelection(TextRange(CAP / 2))
        field.performTextInput("Q")

        assertEquals(full, field.text())
        assertEquals(TextRange(CAP / 2), field.selection())
    }

    @Test
    fun givenOneUnitLeftWhenTypingAnEmojiThenNoHalfIsKept() {
        showDialog()
        val almostFull = "a".repeat(CAP - 1)
        field.performTextInput(almostFull)

        field.performTextInput(THUMBS_UP)

        assertEquals(almostFull, field.text())
    }

    @Test
    fun givenAPasteAcrossTheCapThenItIsCutAfterAWholeCharacter() {
        showDialog()
        val start = "a".repeat(CAP - 4)
        field.performTextInput(start)

        field.performTextInput("abc$THUMBS_UP$THUMBS_UP")

        assertEquals(start + "abc", field.text())
        assertEquals(TextRange(CAP - 1), field.selection())
    }

    @Test
    fun whenTheScreenIsRecreatedThenTextAndCursorAreRestored() {
        val restoration = StateRestorationTester(composeTestRule)
        restoration.setContent { Dialog() }
        field.performTextInput("migraine")
        field.performTextInputSelection(TextRange(3))

        restoration.emulateSavedInstanceStateRestore()

        assertEquals("migraine", field.text())
        assertEquals(TextRange(3), field.selection())
    }

    @Test
    fun whenAddingThenTheTypedNameIsPassedTrimmed() {
        showDialog()
        field.performTextInput("  Jaw pain ")

        // Its click action: a touch would not answer here at any delay. A touch in this test runs
        // on the test's clock, and the dialog reads SystemClock, so it is always inside the wait.
        composeTestRule.onNodeWithText("Add").performSemanticsAction(SemanticsActions.OnClick)

        assertEquals("Jaw pain", added)
    }
}

private fun SemanticsNodeInteraction.text(): String =
    fetchSemanticsNode().config[SemanticsProperties.EditableText].text

private fun SemanticsNodeInteraction.selection(): TextRange =
    fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange]
