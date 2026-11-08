package com.migrely.app.medication.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextInputSelection
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.medication.ui.model.MedicationFormUiModel
import com.migrely.app.medication.ui.model.MedicationKindUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val DOSE_CAP = 20
private const val TAP_INSIDE_LEFT_EDGE_DP = 20

/**
 * What the edit form shows is what it saves, also when two events arrive before the next
 * recomposition (review of #91). Save reads the form, so each test compares the form to the field.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h1200dp")
class EditMedicationFormTest {
    @get:Rule
    val rule = createComposeRule()

    private var form by mutableStateOf<MedicationFormUiModel?>(
        MedicationFormUiModel(kind = MedicationKindUiModel.Preventive)
    )
    private var formChanges = 0

    private val nameField get() = rule.onAllNodes(hasSetTextAction())[0]
    private val doseField get() = rule.onAllNodes(hasSetTextAction())[1]
    private val savedName get() = form?.name
    private val savedDose get() = form?.dose

    @Composable
    private fun EditForm() = MigrelyTheme {
        EditMedicationContents(
            adding = false,
            notFound = false,
            form = form,
            problem = null,
            pending = false,
            onFormChange = {
                formChanges++
                form = it
            },
            onSave = {},
            onBack = {},
            messages = {}
        )
    }

    @Test
    fun givenTwoFramesWhenDeletingAndRetypingThenNameIsSaved() {
        rule.setContent { EditForm() }
        nameField.performTextInput("abc")

        rule.runOnUiThread { nameField.setText()("ab") }
        rule.waitForIdle()
        rule.runOnUiThread { nameField.setText()("abc") }
        rule.waitForIdle()

        assertEquals("abc", nameField.shown())
        assertEquals("abc", savedName)
    }

    @Test
    fun givenOneFrameWhenDeletingAndRetypingTheNameThenShownNameIsSavedName() {
        assertDeleteAndRetypeInOneFrameKeepsShownAndSavedTogether({ nameField }, { savedName })
    }

    @Test
    fun givenOneFrameWhenDeletingAndRetypingTheDoseThenShownDoseIsSavedDose() {
        assertDeleteAndRetypeInOneFrameKeepsShownAndSavedTogether({ doseField }, { savedDose })
    }

    @Test
    fun givenOneFrameWhenTypingInTheNameAndTappingAKindThenShownNameIsSavedName() {
        assertTypingAndKindTapInOneFrameKeepsShownAndSavedTogether({ nameField }, { savedName })
    }

    @Test
    fun givenOneFrameWhenTypingInTheDoseAndTappingAKindThenShownDoseIsSavedDose() {
        assertTypingAndKindTapInOneFrameKeepsShownAndSavedTogether({ doseField }, { savedDose })
    }

    @Test
    fun whenMovingTheCursorInTheNameThenTheFormIsLeftAlone() {
        rule.setContent { EditForm() }
        nameField.performTextInput("abc")
        val changes = formChanges

        nameField.performTextInputSelection(TextRange(1))

        assertEquals(changes, formChanges)
        assertEquals(TextRange(1), nameField.selection())
    }

    @Test
    fun whenMovingTheCursorInTheDoseThenTheFormIsLeftAlone() {
        rule.setContent { EditForm() }
        doseField.performTextInput("400 mg")
        val changes = formChanges

        doseField.performTextInputSelection(TextRange(1))

        assertEquals(changes, formChanges)
        assertEquals(TextRange(1), doseField.selection())
    }

    @Test
    fun givenAFullDoseWhenAnEditIsRefusedThenTheFormIsLeftAlone() {
        rule.setContent { EditForm() }
        val full = "a".repeat(DOSE_CAP)
        doseField.performTextInput(full)
        val changes = formChanges

        doseField.performTextInput("Q")

        assertEquals(changes, formChanges)
        assertEquals(full, savedDose)
    }

    @Test
    fun givenNoFormYetWhenTheFirstLoadArrivesThenStoredTextOpensWithTheCursorAtTheStart() {
        form = null
        rule.setContent { EditForm() }
        rule.waitForIdle()
        assertEquals(0, rule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().size)

        form = MedicationFormUiModel(name = "Ibuprofen", dose = "400 mg")
        rule.waitForIdle()

        assertEquals("Ibuprofen", nameField.shown())
        assertEquals(TextRange(0), nameField.selection())
        assertEquals("400 mg", doseField.shown())
        assertEquals(TextRange(0), doseField.selection())
    }

    @Test
    fun givenTheFirstLoadWhenTypingThenTheStoredTextIsKeptAndChanged() {
        form = null
        rule.setContent { EditForm() }
        form = MedicationFormUiModel(name = "Ibuprofen", dose = "400 mg")
        rule.waitForIdle()

        nameField.performTextInputSelection(TextRange("Ibuprofen".length))
        nameField.performTextInput(" 400")
        doseField.performTextInputSelection(TextRange("400 mg".length))
        doseField.performTextInput(" twice")

        assertEquals("Ibuprofen 400", savedName)
        assertEquals("400 mg twice", savedDose)
    }

    /** A person recognises a medication by the start of its name, so that part must be on screen. */
    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    @Config(sdk = [35], qualifiers = "w400dp-h800dp-xxhdpi")
    fun givenANameWiderThanTheFieldWhenOpenedThenItsStartIsOnScreen() {
        form = MedicationFormUiModel(
            name = "Paracetamol with caffeine and codeine combination 500 mg XX",
            kind = MedicationKindUiModel.Preventive
        )
        rule.setContent { EditForm() }
        rule.waitForIdle()
        val layouts = mutableListOf<TextLayoutResult>()
        val node = nameField.fetchSemanticsNode()
        node.config[SemanticsActions.GetTextLayoutResult].action!!(layouts)
        assertTrue(layouts.first().size.width > node.size.width)
        assertEquals(TextRange(0), nameField.selection())

        nameField.performTouchInput {
            click(Offset(TAP_INSIDE_LEFT_EDGE_DP.dp.toPx(), height / 2f))
        }

        // Scrolled to its end, the same tap lands about 20 characters in.
        assertTrue(nameField.selection().start <= 1)
    }

    private fun assertDeleteAndRetypeInOneFrameKeepsShownAndSavedTogether(
        field: () -> SemanticsNodeInteraction,
        saved: () -> String?
    ) {
        rule.setContent { EditForm() }
        field().performTextInput("abc")
        field().performTextInputSelection(TextRange(1))
        rule.waitForIdle()
        val setText = field().setText()

        rule.runOnUiThread {
            setText("ab")
            setText("abc")
        }
        rule.waitForIdle()

        assertEquals(saved(), field().shown())
    }

    private fun assertTypingAndKindTapInOneFrameKeepsShownAndSavedTogether(
        field: () -> SemanticsNodeInteraction,
        saved: () -> String?
    ) {
        rule.setContent { EditForm() }
        field().performTextInput("abc")
        rule.waitForIdle()
        val setText = field().setText()
        val tapKind = rule.onNodeWithText("For an attack")
            .fetchSemanticsNode().config[SemanticsActions.OnClick].action!!

        rule.runOnUiThread {
            setText("abcd")
            tapKind()
        }
        rule.waitForIdle()

        assertEquals(saved(), field().shown())
        assertEquals(MedicationKindUiModel.Acute, form?.kind)

        // A later edit still reaches the form, even when it brings back the same letter.
        field().performTextInput("d")
        assertEquals("abcd", field().shown())
        assertEquals("abcd", saved())
    }
}

private fun SemanticsNodeInteraction.setText(): (String) -> Boolean {
    val action = fetchSemanticsNode().config[SemanticsActions.SetText].action!!
    return { text -> action(AnnotatedString(text)) }
}

private fun SemanticsNodeInteraction.shown(): String =
    fetchSemanticsNode().config[SemanticsProperties.EditableText].text

private fun SemanticsNodeInteraction.selection(): TextRange =
    fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange]
