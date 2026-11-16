package com.migrely.app.history.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.migrely.app.designsystem.snackbar.LocalMessageClock
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.history.ui.mapper.HistoryMessageBehaviourUiMapper
import com.migrely.app.history.ui.model.HistoryMessageUiModel
import com.migrely.app.history.ui.notification.HistoryMessagePresenter
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val STORAGE_FULL = "Couldn't save. Your phone's storage is full."
private const val SCREEN = "screen"
private const val OTHER = "other"
private const val UNDO_DELETE = "Undo the deletion"
private const val UNDONE = "Undone."
private const val UNDO_NOT_POSSIBLE = "This can no longer be undone."

// EC-ATK-24: "Time to take action" at 2 minutes, a deletion History offers 20 s after it was made, and D23's 2 minutes
// in which it can be restored.
private const val TIME_TO_TAKE_ACTION_MILLIS = 120_000L
private const val OFFERED_AFTER_MILLIS = 20_000L
private const val RESTORABLE_MILLIS = 120_000L

// A few frames on the held Compose clock, far shorter than any time a message has left.
private const val FRAMES_MILLIS = 100L

/** History after D178: "Saved." replaces an error and nothing else, and an error belongs to the visit (BRD EC-UX-35). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HistoryMessagesTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val presenter = HistoryMessagePresenter()
    private lateinit var navController: NavHostController
    private val clock = FakeMessageClock(wall = System.currentTimeMillis())
    private var deletedAt = 0L

    private fun showScreen(vararg locals: ProvidedValue<*>) {
        rule.setContent {
            navController = rememberNavController()
            MigrelyTheme {
                NavHost(navController, startDestination = SCREEN) {
                    composable(SCREEN) {
                        CompositionLocalProvider(*locals) {
                            HistoryMessages(
                                presenter = presenter,
                                navController = navController,
                                behaviourMapper = HistoryMessageBehaviourUiMapper(),
                                reduceMotion = true,
                                onUndoDelete = { restoreDeletion() }
                            )
                        }
                    }
                    composable(OTHER) { Text(OTHER) }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun post(message: HistoryMessageUiModel) {
        rule.runOnIdle { presenter.show(message) }
        rule.waitForIdle()
    }

    /** D23's restore as History's view model reports it: back within its 2 minutes, purged after them. */
    private fun restoreDeletion() {
        presenter.show(
            if (clock.wall - deletedAt <= RESTORABLE_MILLIS) {
                HistoryMessageUiModel.Undone
            } else {
                HistoryMessageUiModel.UndoNotPossible
            }
        )
    }

    private fun showScreenWithLongTimeToTakeAction() = showScreen(
        LocalAccessibilityManager provides FakeAccessibilityManager(TIME_TO_TAKE_ACTION_MILLIS),
        LocalMessageClock provides clock
    )

    /**
     * A migraine deleted on Details opened from History, which History offers 20 s later. Once it shows, the Compose
     * clock is held, so only the phone's clocks move, as while the screen is locked. The deletion is stamped on the
     * phone's wall clock, which History reads when a deletion arrives.
     */
    private fun offerDeletionLate() {
        deletedAt = System.currentTimeMillis().also { clock.wall = it }
        clock.advanceBy(OFFERED_AFTER_MILLIS)
        post(HistoryMessageUiModel.AttackDeleted("a", deletedAt))
        rule.onNodeWithContentDescription(UNDO_DELETE).assertExists()
        rule.mainClock.autoAdvance = false
    }

    private fun nextFrames() {
        rule.mainClock.advanceTimeBy(FRAMES_MILLIS)
        rule.waitForIdle()
    }

    /** Locks the screen and unlocks it [millisAfterDeletion] after the deletion. */
    private fun lockAndUnlockAt(millisAfterDeletion: Long) {
        rule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        clock.advanceBy(deletedAt + millisAfterDeletion - clock.wall)
        rule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        nextFrames()
    }

    private fun goForwardAndBack() {
        rule.runOnIdle { navController.navigate(OTHER) }
        rule.waitForIdle()
        rule.runOnIdle { navController.popBackStack() }
        rule.waitForIdle()
    }

    @Test
    fun `A write saved after an error shows Saved in its place`() {
        showScreen()
        post(HistoryMessageUiModel.StorageFull)

        post(HistoryMessageUiModel.Saved)

        rule.onNodeWithText("Saved.").assertExists()
        rule.onNodeWithText(STORAGE_FULL).assertDoesNotExist()
    }

    @Test
    fun `A write saved with no error shows nothing`() {
        showScreen()

        post(HistoryMessageUiModel.Saved)

        rule.onNodeWithText("Saved.").assertDoesNotExist()
    }

    @Test
    fun `A write saved while Undo shows leaves Undo alone`() {
        showScreen()
        post(HistoryMessageUiModel.AttackDeleted("a", System.currentTimeMillis()))

        post(HistoryMessageUiModel.Saved)

        rule.onNodeWithText("Migraine deleted").assertExists()
        rule.onNodeWithText("Saved.").assertDoesNotExist()
    }

    @Test
    fun `An error is gone after going forward and back`() {
        showScreen()
        post(HistoryMessageUiModel.StorageFull)

        goForwardAndBack()

        rule.onNodeWithText(STORAGE_FULL).assertDoesNotExist()
    }

    @Test
    fun `An error stays when the app goes to the background and comes back`() {
        showScreen()
        post(HistoryMessageUiModel.StorageFull)

        rule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        rule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        rule.waitForIdle()

        rule.onNodeWithText(STORAGE_FULL).assertExists()
    }

    @Test
    fun `Its own message after an error replaces it`() {
        showScreen()
        post(HistoryMessageUiModel.StorageFull)

        post(HistoryMessageUiModel.AttackDeleted("a", System.currentTimeMillis()))

        rule.onNodeWithText("Migraine deleted").assertExists()
        rule.onNodeWithText(STORAGE_FULL).assertDoesNotExist()
    }

    @Test
    fun `A message with Undo stays across a trip`() {
        showScreen()
        post(HistoryMessageUiModel.AttackDeleted("a", System.currentTimeMillis()))

        goForwardAndBack()

        rule.onNodeWithText("Migraine deleted").assertExists()
    }

    // The Undo ceiling (EC-ATK-24, BRD D182 (3))

    @Test
    fun `Lock and unlock at 122 seconds on History leaves no Undo`() {
        showScreenWithLongTimeToTakeAction()
        offerDeletionLate()

        lockAndUnlockAt(122_000)

        rule.onNodeWithContentDescription(UNDO_DELETE).assertDoesNotExist()
        rule.onNodeWithText(UNDO_NOT_POSSIBLE).assertDoesNotExist()
    }

    @Test
    fun `Undo before the ceiling still restores on History`() {
        showScreenWithLongTimeToTakeAction()
        offerDeletionLate()
        lockAndUnlockAt(114_000)

        rule.onNodeWithContentDescription(UNDO_DELETE).performClick()
        nextFrames()

        rule.onNodeWithText(UNDONE).assertExists()
        rule.onNodeWithText(UNDO_NOT_POSSIBLE).assertDoesNotExist()
    }
}
