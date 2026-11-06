package com.migrely.app.medication.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performTouchInput
import com.migrely.app.medication.ui.model.MedicationCategoryUiModel
import com.migrely.app.medication.ui.model.MedicationFormUiModel
import com.migrely.app.medication.ui.model.MedicationKindUiModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val PREVENTIVE = "Preventive"
private const val TRIPTAN = "Triptan"
private const val BACK = "Back"
private const val SAVE = "Save"
private const val JUST_OPENED_MILLIS = 100L
private const val AT_THE_MEASURED_GAP_MILLIS = 649L
private const val AFTER_THE_GUARD_MILLIS = 800L

/**
 * A second tap meant for the list lands on the editor that has just opened (issue #140). It must
 * not flip the kind or the category, on Add or Edit, up to the gaps measured. Everything else takes
 * its first tap at once (issue #148).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h1200dp")
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
    fun aTapOnEditAtTheMeasuredGapDoesNotChangeTheKind() {
        show()
        advance(AT_THE_MEASURED_GAP_MILLIS)

        tap(PREVENTIVE)

        composeRule.runOnIdle { assertEquals(MedicationKindUiModel.Acute, form.kind) }
    }

    @Test
    fun aTapOnAddAtTheMeasuredGapDoesNotChangeTheKind() {
        adding = true
        show()
        advance(AT_THE_MEASURED_GAP_MILLIS)

        tap(PREVENTIVE)

        composeRule.runOnIdle { assertEquals(MedicationKindUiModel.Acute, form.kind) }
    }

    @Test
    fun aTapAtTheMeasuredGapDoesNotChangeTheCategory() {
        show()
        advance(AT_THE_MEASURED_GAP_MILLIS)

        tap(TRIPTAN)

        composeRule.runOnIdle {
            assertEquals(MedicationCategoryUiModel.SimplePainkiller, form.category)
        }
    }

    @Test
    fun theKindAndCategoryTakeATapAfterTheGuard() {
        show()
        advance(AFTER_THE_GUARD_MILLIS)

        tap(TRIPTAN)
        composeRule.runOnIdle { assertEquals(MedicationCategoryUiModel.Triptan, form.category) }

        tap(PREVENTIVE)
        composeRule.runOnIdle { assertEquals(MedicationKindUiModel.Preventive, form.kind) }
    }

    @Test
    fun theNameTakesAFirstTapJustAfterOpening() {
        show()
        advance(JUST_OPENED_MILLIS)

        tapField(index = 0)

        composeRule.onAllNodes(hasSetTextAction())[0].assertIsFocused()
    }

    @Test
    fun theDoseTakesAFirstTapJustAfterOpening() {
        show()
        advance(JUST_OPENED_MILLIS)

        tapField(index = 1)

        composeRule.onAllNodes(hasSetTextAction())[1].assertIsFocused()
    }

    @Test
    fun saveTakesAFirstTapJustAfterOpening() {
        show()
        advance(JUST_OPENED_MILLIS)

        tap(SAVE)

        composeRule.runOnIdle { assertEquals(1, saves.intValue) }
    }

    @Test
    fun backTakesAFirstTapJustAfterOpening() {
        show()
        advance(JUST_OPENED_MILLIS)

        composeRule.onNode(hasContentDescription(BACK)).performTouchInput {
            down(center)
            up()
        }
        composeRule.waitForIdle()

        composeRule.runOnIdle { assertEquals(1, backs.intValue) }
    }

    private fun tapField(index: Int) {
        composeRule.onAllNodes(hasSetTextAction())[index].performTouchInput {
            down(center)
            up()
        }
        advance(JUST_OPENED_MILLIS)
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
