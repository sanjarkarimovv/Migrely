package com.migrely.app.medication.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import com.migrely.app.medication.ui.model.MedicationCategoryUiModel
import com.migrely.app.medication.ui.model.MedicationFormUiModel
import com.migrely.app.medication.ui.model.MedicationKindUiModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private const val PREVENTIVE = "Preventive"
private const val ACUTE = "For an attack"
private const val BACK = "Back"
private const val SAVE = "Save"
private const val STILL_FADING_MILLIS = 400L
private const val AFTER_THE_FADE_MILLIS = 800L

/**
 * A second tap meant for the list lands on the editor that has just opened (issue #140). It must
 * not change the form, for adding or for editing; Back stays free, and the form works afterwards.
 */
class EditMedicationTapGuardTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var form by mutableStateOf(
        MedicationFormUiModel(
            "Alphaprofen",
            MedicationKindUiModel.Acute,
            MedicationCategoryUiModel.SimplePainkiller,
            "400 mg"
        )
    )
    private val backs = mutableIntStateOf(0)
    private val saves = mutableIntStateOf(0)
    private var adding = false

    @Before
    fun stopTheClock() {
        composeRule.mainClock.autoAdvance = false
    }

    private fun show() {
        composeRule.setContent {
            MaterialTheme {
                EditMedicationContents(
                    adding = adding,
                    notFound = false,
                    form = form,
                    problem = null,
                    pending = false,
                    onFormChange = { form = it },
                    onSave = { saves.intValue++ },
                    onBack = { backs.intValue++ },
                    messages = {}
                )
            }
        }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()
    }

    @Test
    fun aTapOnEditWhileItFadesInDoesNotChangeTheKind() {
        show()
        advance(STILL_FADING_MILLIS)

        tap(PREVENTIVE)

        composeRule.runOnIdle { assertEquals(MedicationKindUiModel.Acute, form.kind) }
    }

    @Test
    fun aTapOnAddWhileItFadesInDoesNotChangeTheKind() {
        adding = true
        show()
        advance(STILL_FADING_MILLIS)

        tap(PREVENTIVE)

        composeRule.runOnIdle { assertEquals(MedicationKindUiModel.Acute, form.kind) }
    }

    @Test
    fun aTapOnSaveWhileItFadesInDoesNotSave() {
        show()
        advance(STILL_FADING_MILLIS)

        tap(SAVE)

        composeRule.runOnIdle { assertEquals(0, saves.intValue) }
    }

    @Test
    fun backStaysFreeWhileItFadesIn() {
        show()
        advance(STILL_FADING_MILLIS)

        composeRule.onNode(hasContentDescription(BACK)).performTouchInput {
            down(center)
            up()
        }
        composeRule.waitForIdle()

        composeRule.runOnIdle { assertEquals(1, backs.intValue) }
    }

    @Test
    fun theFormTakesATapAfterTheFade() {
        show()
        advance(AFTER_THE_FADE_MILLIS)

        tap(PREVENTIVE)

        composeRule.runOnIdle { assertEquals(MedicationKindUiModel.Preventive, form.kind) }
        composeRule.onNodeWithText(ACUTE).assertExists()
    }

    private fun tap(label: String) {
        composeRule.onNode(hasText(label), useUnmergedTree = true).performTouchInput {
            down(center)
            up()
        }
        composeRule.waitForIdle()
    }

    private fun advance(millis: Long) {
        composeRule.mainClock.advanceTimeBy(millis)
        composeRule.waitForIdle()
    }
}
