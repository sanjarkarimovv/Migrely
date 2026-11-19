package com.migrely.app.medication.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.migrely.app.designsystem.snackbar.LocalMessageClock
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.medication.ui.mapper.MedicationMessageBehaviourUiMapper
import com.migrely.app.medication.ui.model.MedicationEventUiModel
import com.migrely.app.medication.ui.model.MedicationMessageUiModel
import com.migrely.app.medication.ui.model.SaveProblemUiModel
import com.migrely.app.medication.ui.notification.MedicationEventPresenter
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val STORAGE_FULL = "Couldn't save. Your phone's storage is full."
private const val SCREEN = "screen"
private const val OTHER = "other"
private const val INTAKE_UNDONE = "Removed. Nothing was logged."
private const val LIMIT_REACHED =
    "You can keep up to 50 medications. Remove one you no longer take."

// D182 (1): "Time to take action" and the reading timeout set apart.
private const val TIME_TO_TAKE_ACTION_MILLIS = 20_000L
private const val READING_TIMEOUT_MILLIS = 8_000L

/** The medication screens after D178: an error belongs to the visit, and their own message replaces it (BRD EC-UX-35). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MedicationMessagesTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val presenter = MedicationEventPresenter()
    private lateinit var navController: NavHostController
    private val clock = FakeMessageClock(wall = System.currentTimeMillis())

    private fun showScreen(vararg locals: ProvidedValue<*>) {
        rule.setContent {
            navController = rememberNavController()
            MigrelyTheme {
                NavHost(navController, startDestination = SCREEN) {
                    composable(SCREEN) {
                        CompositionLocalProvider(*locals) {
                            MedicationMessages(
                                presenter = presenter,
                                navController = navController,
                                behaviourMapper = MedicationMessageBehaviourUiMapper(),
                                reduceMotion = true
                            )
                        }
                    }
                    composable(OTHER) { Text(OTHER) }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun post(message: MedicationMessageUiModel) {
        rule.runOnIdle { presenter.show(MedicationEventUiModel.Message(message)) }
        rule.waitForIdle()
    }

    /** Time passes on the screen: the Compose clock and the phone's clocks together. */
    private fun passTime(millis: Long) {
        clock.advanceBy(millis)
        rule.mainClock.advanceTimeBy(millis)
        rule.waitForIdle()
    }

    private fun goForwardAndBack() {
        rule.runOnIdle { navController.navigate(OTHER) }
        rule.waitForIdle()
        rule.runOnIdle { navController.popBackStack() }
        rule.waitForIdle()
    }

    @Test
    fun `An error is gone after going forward and back`() {
        showScreen()
        post(MedicationMessageUiModel.StorageFull)

        goForwardAndBack()

        rule.onNodeWithText(STORAGE_FULL).assertDoesNotExist()
    }

    @Test
    fun `An error stays when the app goes to the background and comes back`() {
        showScreen()
        post(MedicationMessageUiModel.StorageFull)

        rule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        rule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        rule.waitForIdle()

        rule.onNodeWithText(STORAGE_FULL).assertExists()
    }

    @Test
    fun `Its own message after an error replaces it`() {
        showScreen()
        post(MedicationMessageUiModel.StorageFull)

        post(MedicationMessageUiModel.Archived("i"))

        rule.onNodeWithText("Medication removed").assertExists()
        rule.onNodeWithText(STORAGE_FULL).assertDoesNotExist()
    }

    @Test
    fun `A message with Undo stays across a trip`() {
        showScreen()
        post(MedicationMessageUiModel.Archived("i"))

        goForwardAndBack()

        rule.onNodeWithText("Medication removed").assertExists()
    }

    // A field problem clears an older message, silently and at once (BRD D191, ux F9)

    private fun clearsOnAFieldProblem(message: MedicationMessageUiModel, text: String) {
        val haptics = RecordingHaptics()
        showScreen(LocalHapticFeedback provides haptics)
        post(message)
        rule.onNodeWithText(text).assertExists()
        val buzzes = haptics.count
        rule.mainClock.autoAdvance = false

        rule.runOnIdle {
            presenter.show(MedicationEventUiModel.NotSaved(SaveProblemUiModel.InvalidName))
        }
        rule.mainClock.advanceTimeByFrame()
        rule.mainClock.advanceTimeByFrame()

        rule.onNodeWithText(text).assertDoesNotExist()
        assertEquals("a haptic for the field problem", buzzes, haptics.count)
        rule.mainClock.autoAdvance = true
    }

    @Test
    fun `A field problem clears the storage error at once, without haptic, then one shows`() {
        clearsOnAFieldProblem(MedicationMessageUiModel.StorageFull, STORAGE_FULL)

        post(MedicationMessageUiModel.StorageFull)

        rule.onNodeWithText(STORAGE_FULL).assertExists()
    }

    @Test
    fun `A field problem clears the 50 medications message`() =
        clearsOnAFieldProblem(MedicationMessageUiModel.LimitReached, LIMIT_REACHED)

    // One timeout for every message (EC-UX-52, BRD D182 (1))

    @Test
    fun `A removed intake on Log medication stays for the time to take action`() {
        showScreen(
            LocalAccessibilityManager provides FakeAccessibilityManager(
                interactiveMillis = TIME_TO_TAKE_ACTION_MILLIS,
                nonInteractiveMillis = READING_TIMEOUT_MILLIS
            ),
            LocalMessageClock provides clock
        )
        post(MedicationMessageUiModel.IntakeUndone)

        passTime(19_000)
        rule.onNodeWithText(INTAKE_UNDONE).assertExists()

        // Past the 20 s, with room for the exit.
        passTime(3_000)
        rule.onNodeWithText(INTAKE_UNDONE).assertDoesNotExist()
    }
}

/** Counts the haptics a screen asks for. */
private class RecordingHaptics : HapticFeedback {
    var count = 0
        private set

    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
        count++
    }
}
