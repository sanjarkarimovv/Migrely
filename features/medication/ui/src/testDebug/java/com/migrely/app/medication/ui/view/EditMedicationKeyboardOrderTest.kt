package com.migrely.app.medication.ui.view

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.requestFocus
import com.migrely.app.designsystem.snackbar.MigrelySnackbar
import com.migrely.app.designsystem.snackbar.MigrelySnackbarHost
import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.medication.ui.model.MedicationFormUiModel
import com.migrely.app.medication.ui.model.MedicationKindUiModel
import com.migrely.app.medication.ui.model.SaveProblemUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val BACK = "Back"
private const val NAME = "Name"
private const val ACUTE = "For an attack"
private const val PREVENTIVE = "Preventive"
private const val DOSE = "Usual dose (optional)"
private const val SAVE = "Save"
private const val DISMISS = "Dismiss"

/** Add with "For an attack" chosen, after Back (BRD EC-UX-49). */
private val ADD_ORDER = listOf(
    NAME,
    ACUTE,
    PREVENTIVE,
    "Simple painkiller",
    "Triptan",
    "Combination or other",
    "Not sure",
    DOSE,
    SAVE
)

/**
 * Edit medication with a hardware keyboard (BRD D181 (6), EC-UX-49): the order and its reverse,
 * options that focus never selects, and focus that follows a Save problem in keyboard mode only.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w400dp-h1400dp")
class EditMedicationKeyboardOrderTest {
    @get:Rule
    val rule = createComposeRule()

    private var form by mutableStateOf(MedicationFormUiModel(kind = MedicationKindUiModel.Acute))
    private var problem by mutableStateOf<SaveProblemUiModel?>(null)
    private var pending by mutableStateOf(false)
    private var notFound by mutableStateOf(false)

    /** What the next Save finds, as the ViewModel's NotSaved event would say. */
    private var problemOnSave: SaveProblemUiModel? = null
    private var savePends = false

    private var touchOnly = false

    private fun show(withMessage: Boolean = false) = rule.setContent {
        val inputMode = if (touchOnly) TouchOnly else LocalInputModeManager.current
        CompositionLocalProvider(LocalInputModeManager provides inputMode) { Screen(withMessage) }
    }

    @Composable
    private fun Screen(withMessage: Boolean) {
        MigrelyTheme {
            EditMedicationContents(
                adding = true,
                notFound = notFound,
                form = if (notFound) null else form,
                problem = problem,
                pending = pending,
                onFormChange = {
                    form = it
                    problem = null
                },
                onSave = {
                    if (savePends) pending = true else problem = problemOnSave
                },
                onBack = {},
                messages = {
                    if (withMessage) {
                        MigrelySnackbarHost(message = "Couldn't save.", reduceMotion = true) {
                            MigrelySnackbar(it, SnackbarTone.Error, null, null, DISMISS, {})
                        }
                    }
                }
            )
        }
    }

    private fun focusOn(text: String) = rule.onNodeWithText(text).requestFocus()

    private fun focusOnBack() = rule.onNodeWithContentDescription(BACK).requestFocus()

    @Test
    fun `Tab runs the full order from Back, and Shift+Tab reverses it`() {
        show()
        focusOnBack()

        assertEquals(ADD_ORDER, rule.walk(ADD_ORDER.size))
        assertEquals(
            ADD_ORDER.reversed().drop(1) + BACK,
            rule.walk(ADD_ORDER.size, back = true)
        )
    }

    /** Negative control (task 5.1): an expected list with one stop dropped does not match. */
    @Test
    fun `The order check fails when a stop is missing`() {
        show()
        focusOnBack()

        assertNotEquals(ADD_ORDER - "Triptan", rule.walk(ADD_ORDER.size))
    }

    @Test
    fun `With a message, its control comes after Back, then Name`() {
        show(withMessage = true)
        focusOnBack()

        assertEquals(listOf(DISMISS, NAME), rule.walk(2))
    }

    @Test
    fun `The down arrow moves focus without selecting`() {
        show()
        focusOn(ACUTE)

        rule.press(Key.DirectionDown)

        assertEquals(PREVENTIVE, rule.focusedName())
        assertEquals(MedicationKindUiModel.Acute, form.kind)
    }

    @Test
    fun `Space on Preventive keeps focus there, and the next Tab goes to Usual dose`() {
        show()
        focusOn(PREVENTIVE)

        rule.press(Key.Spacebar)
        assertEquals(MedicationKindUiModel.Preventive, form.kind)
        assertEquals(PREVENTIVE, rule.focusedName())
        assertEquals(listOf(DOSE), rule.walk(1))

        // And back to "For an attack": its types come back after Preventive.
        rule.walk(2, back = true)
        rule.press(Key.Spacebar)
        assertEquals(MedicationKindUiModel.Acute, form.kind)
        assertEquals(listOf(PREVENTIVE, "Simple painkiller"), rule.walk(2))
    }

    @Test
    fun `Tab in Name moves on and types no tab`() {
        show()
        focusOnBack()
        rule.walk(1)

        rule.press(Key.Tab)

        assertEquals(ACUTE, rule.focusedName())
        assertEquals("", form.name)
    }

    @Test
    fun `Enter on Save clears focus while it waits, and the next Tab goes to Back`() {
        show()
        focusOnBack()
        rule.walk(ADD_ORDER.size)

        // As the screen does: Save turns pending and waits.
        savePends = true
        rule.press(Key.Enter)

        assertEquals(true, pending)
        assertNull(rule.focusedName())
        assertEquals(listOf(BACK), rule.walk(1))
    }

    @Test
    fun `When not found, Back is the only stop`() {
        notFound = true
        show()
        focusOnBack()

        assertEquals(listOf(BACK, BACK), rule.walk(2))
    }

    @Test
    fun `Enter on Save with a duplicate name moves keyboard focus to Name`() {
        show()
        problemOnSave = SaveProblemUiModel.Duplicate("Ibuprofen")
        focusOnBack()
        rule.walk(ADD_ORDER.size)

        rule.press(Key.Enter)

        assertEquals(NAME, rule.focusedName())
    }

    @Test
    fun `Enter on Save with an invalid dose moves keyboard focus to Usual dose`() {
        show()
        problemOnSave = SaveProblemUiModel.InvalidDose
        focusOnBack()
        rule.walk(ADD_ORDER.size)

        rule.press(Key.Enter)

        assertEquals(DOSE, rule.focusedName())
    }

    /** Robolectric's view never stays in touch mode, so the screen is given a touch-only manager. */
    @Test
    fun `A touch on Save with a duplicate name moves no focus`() {
        touchOnly = true
        show()
        problemOnSave = SaveProblemUiModel.Duplicate("Ibuprofen")

        rule.onNodeWithText(SAVE).performClick()
        rule.waitForIdle()

        assertEquals(problemOnSave, problem)
        assertNull(rule.focusedName())
    }
}

private object TouchOnly : InputModeManager {
    override val inputMode = InputMode.Touch

    @ExperimentalComposeUiApi
    override fun requestInputMode(inputMode: InputMode) = false
}
