package com.migrely.app.medication.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.migrely.app.medication.domain.model.MedicationWriteResultDomainModel
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val NEW_NAME = "Naproxen"

/**
 * On both medication forms a field problem clears an older message (BRD D191, ux F9): the storage error of an earlier
 * attempt goes when Save finds an empty name, the field error shows, and a restored screen keeps it that way without
 * a new announcement (EC-UX-70).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class MedicationFormMessagesTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val screen = ProblemFeedbackHarness(rule, FakeDiary())
    private val name get() = rule.onNode(nameField)

    private fun press(button: String) {
        rule.onNode(hasScrollAction()).performScrollToNode(hasText(button))
        rule.onNodeWithText(button).performClick()
    }

    /** A full disk refuses [NEW_NAME]; then the name is cleared and [button] finds it empty. */
    private fun storageErrorThenEmptyName(button: String) {
        name.performTextInput(NEW_NAME)
        screen.diary.createFailure = MedicationWriteResultDomainModel.StorageFull
        press(button)
        screen.awaitNode(hasText(STORAGE_FULL_TEXT))
        screen.diary.createFailure = null
        name.performTextClearance()

        press(button)
        screen.awaitProblem(ENTER_A_NAME)
        rule.waitForIdle()
    }

    @Test
    fun `On Edit medication an empty name clears the storage error`() {
        screen.showEdit()

        storageErrorThenEmptyName("Save")

        rule.onNodeWithText(STORAGE_FULL_TEXT).assertDoesNotExist()
    }

    @Test
    fun `On Log medication's first use an empty name clears the storage error`() {
        screen.showFirstUse()

        storageErrorThenEmptyName("Add and log")

        rule.onNodeWithText(STORAGE_FULL_TEXT).assertDoesNotExist()
    }

    @Test
    fun `A restored screen after the clear shows the field error, no message and no new announcement`() {
        val restoration = StateRestorationTester(rule)
        screen.showEdit(restoration = restoration)
        storageErrorThenEmptyName("Save")
        rule.mainClock.advanceTimeBy(PAST_ANNOUNCEMENT_MILLIS)
        rule.waitForIdle()

        restoration.emulateSavedInstanceStateRestore()
        rule.waitForIdle()

        screen.awaitProblem(ENTER_A_NAME)
        rule.onNodeWithText(STORAGE_FULL_TEXT).assertDoesNotExist()
        assertFalse("announced again on restore", screen.exists(announcement(ENTER_A_NAME)))
    }
}
