package com.migrely.app.home.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.migrely.app.home.ui.mapper.HomeMessageBehaviourUiMapper
import com.migrely.app.home.ui.model.HomeMessageUiModel
import com.migrely.app.home.ui.notification.HomeMessagePresenter
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val STORAGE_FULL = "Couldn't save. Your phone's storage is full."
private const val SAVED = "Saved."
private const val ENDED = "Migraine ended"
private const val REVIEW = "review"
private const val HOME = "home"
private const val SETTINGS = "settings"
private const val UNDO_DELETE = "Undo the deletion"
private const val UNDONE = "Undone."
private const val UNDO_NOT_POSSIBLE = "This can no longer be undone."

// EC-ATK-24: "Time to take action" at 2 minutes, a deletion Home offers 20 s after it was made, and D23's 2 minutes
// in which it can be restored.
private const val TIME_TO_TAKE_ACTION_MILLIS = 120_000L
private const val OFFERED_AFTER_MILLIS = 20_000L
private const val RESTORABLE_MILLIS = 120_000L

// D182 (1): "Time to take action" and the reading timeout set apart.
private const val SPLIT_INTERACTIVE_MILLIS = 20_000L
private const val SPLIT_READING_MILLIS = 8_000L

// A few frames on the held Compose clock, far shorter than any time a message has left.
private const val FRAMES_MILLIS = 100L

/**
 * Home's messages after D178: "Saved." replaces an error and nothing else (EC-UX-35), an error belongs to the visit
 * (navigation, not lifecycle), and the review request waits for any message and for the next visit after an error
 * (EC-REV-13, ux S1, S4, R1–R5).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class HomeMessagesTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val presenter = HomeMessagePresenter()
    private lateinit var navController: NavHostController
    private val clock = FakeMessageClock(wall = System.currentTimeMillis())

    // One for the whole test, as the phone has one: a new one would read as a restart.
    private val composeClock by lazy { ComposeMessageClock(rule.mainClock) }
    private var deletedAt = 0L

    /** Home with its messages and its review slot, inside a NavHost with one other screen. */
    private fun showHome(vararg locals: ProvidedValue<*>) {
        rule.setContent {
            navController = rememberNavController()
            MigrelyTheme {
                NavHost(navController, startDestination = HOME) {
                    composable(HOME) {
                        CompositionLocalProvider(
                            LocalMessageClock provides composeClock
                        ) {
                            CompositionLocalProvider(*locals) { HomeWithReview() }
                        }
                    }
                    composable(SETTINGS) { Text(SETTINGS) }
                }
            }
        }
        rule.waitForIdle()
    }

    @Composable
    private fun HomeWithReview() {
        val gate = rememberReviewGate(navController)
        if (gate.open) Text(REVIEW)
        HomeMessages(
            presenter = presenter,
            navController = navController,
            behaviourMapper = HomeMessageBehaviourUiMapper(),
            timeUiFormatter = previewFormatter,
            reduceMotion = true,
            onUndoStart = {},
            onUndoEnd = {},
            onUndoDelete = { restoreDeletion() },
            onMessageBottomChanged = {},
            onShownChanged = gate::onShownChanged,
            onErrorPosted = gate::onErrorPosted
        )
    }

    private fun post(message: HomeMessageUiModel) {
        rule.runOnIdle { presenter.show(message) }
        rule.waitForIdle()
    }

    /** D23's restore as Home's view model reports it: back within its 2 minutes, purged after them. */
    private fun restoreDeletion() {
        presenter.show(
            if (clock.wall - deletedAt <= RESTORABLE_MILLIS) {
                HomeMessageUiModel.Undone
            } else {
                HomeMessageUiModel.UndoNotPossible
            }
        )
    }

    private fun showHomeWithLongTimeToTakeAction() = showHome(
        LocalAccessibilityManager provides FakeAccessibilityManager(TIME_TO_TAKE_ACTION_MILLIS),
        LocalMessageClock provides clock
    )

    /**
     * A migraine deleted on Details, which Home offers 20 s later. Once it shows, the Compose clock is held, so only
     * the phone's clocks move, as while the screen is locked.
     */
    private fun offerDeletionLate() {
        deletedAt = clock.wall
        clock.advanceBy(OFFERED_AFTER_MILLIS)
        post(HomeMessageUiModel.AttackDeleted("a", deletedAt))
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

    private fun goToSettingsAndBack() {
        rule.runOnIdle { navController.navigate(SETTINGS) }
        rule.waitForIdle()
        rule.runOnIdle { navController.popBackStack() }
        rule.waitForIdle()
    }

    // "Saved." (S1, S4)

    @Test
    fun `A write saved after an error shows Saved in its place`() {
        showHome()
        post(HomeMessageUiModel.StorageFull)

        post(HomeMessageUiModel.Saved)

        rule.onNodeWithText(SAVED).assertExists()
        rule.onNodeWithText(STORAGE_FULL).assertDoesNotExist()
    }

    @Test
    fun `A write saved with no error shows nothing`() {
        showHome()

        post(HomeMessageUiModel.Saved)

        rule.onNodeWithText(SAVED).assertDoesNotExist()
    }

    @Test
    fun `A write saved while Undo shows leaves Undo alone`() {
        showHome()
        post(HomeMessageUiModel.AttackEnded("a"))

        post(HomeMessageUiModel.Saved)

        rule.onNodeWithText(ENDED).assertExists()
        rule.onNodeWithText(SAVED).assertDoesNotExist()
    }

    @Test
    fun `Two quick saves after an error show one Saved`() {
        showHome()
        post(HomeMessageUiModel.StorageFull)

        post(HomeMessageUiModel.Saved)
        post(HomeMessageUiModel.Saved)

        rule.onNodeWithText(SAVED).assertExists()
    }

    @Test
    fun `A failed retry keeps the error`() {
        showHome()
        post(HomeMessageUiModel.StorageFull)

        post(HomeMessageUiModel.StorageFull)

        rule.onNodeWithText(STORAGE_FULL).assertExists()
    }

    // The visit (D178 (2))

    @Test
    fun `An error is gone after going to Settings and back`() {
        showHome()
        post(HomeMessageUiModel.StorageFull)

        goToSettingsAndBack()

        rule.onNodeWithText(STORAGE_FULL).assertDoesNotExist()
    }

    @Test
    fun `An error stays when the app goes to the background and comes back`() {
        showHome()
        post(HomeMessageUiModel.StorageFull)

        rule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        rule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        rule.waitForIdle()

        rule.onNodeWithText(STORAGE_FULL).assertExists()
    }

    @Test
    fun `Undo stays across a trip within its time`() {
        showHome()
        post(HomeMessageUiModel.AttackEnded("a"))

        goToSettingsAndBack()

        rule.onNodeWithText(ENDED).assertExists()
    }

    // The review request (R1–R5)

    @Test
    fun `With no message the review slot is there`() {
        showHome()

        rule.onNodeWithText(REVIEW).assertExists()
    }

    @Test
    fun `While an error shows the review slot is gone`() {
        showHome()

        post(HomeMessageUiModel.StorageFull)

        rule.onNodeWithText(REVIEW).assertDoesNotExist()
    }

    @Test
    fun `After an error is dismissed the review slot stays gone for the rest of the visit`() {
        showHome()
        post(HomeMessageUiModel.StorageFull)

        rule.onNodeWithContentDescription("Dismiss").performClick()
        rule.waitForIdle()

        rule.onNodeWithText(STORAGE_FULL).assertDoesNotExist()
        rule.onNodeWithText(REVIEW).assertDoesNotExist()
    }

    @Test
    fun `After an error the review slot comes back on the next visit`() {
        showHome()
        post(HomeMessageUiModel.StorageFull)

        goToSettingsAndBack()

        rule.onNodeWithText(REVIEW).assertExists()
    }

    @Test
    fun `While Undo shows the review slot is gone, and it comes back when Undo leaves`() {
        showHome()
        post(HomeMessageUiModel.AttackEnded("a"))
        rule.onNodeWithText(REVIEW).assertDoesNotExist()

        // Undo's 5 s, with room for Robolectric's slower clock.
        rule.mainClock.advanceTimeBy(12_000)
        rule.waitForIdle()

        rule.onNodeWithText(ENDED).assertDoesNotExist()
        rule.onNodeWithText(REVIEW).assertExists()
    }

    // The Undo ceiling (EC-ATK-24, BRD D182 (3))

    @Test
    fun `Lock and unlock at 122 seconds on Home leaves no Undo`() {
        showHomeWithLongTimeToTakeAction()
        offerDeletionLate()

        lockAndUnlockAt(122_000)

        rule.onNodeWithContentDescription(UNDO_DELETE).assertDoesNotExist()
        rule.onNodeWithText(UNDO_NOT_POSSIBLE).assertDoesNotExist()
    }

    @Test
    fun `Undo before the ceiling still restores on Home`() {
        showHomeWithLongTimeToTakeAction()
        offerDeletionLate()
        lockAndUnlockAt(114_000)

        rule.onNodeWithContentDescription(UNDO_DELETE).performClick()
        nextFrames()

        rule.onNodeWithText(UNDONE).assertExists()
        rule.onNodeWithText(UNDO_NOT_POSSIBLE).assertDoesNotExist()
    }

    // One timeout for every message (EC-UX-52, BRD D182 (1))

    @Test
    fun `Saved stays for the time to take action, not the reading timeout`() {
        showHome(
            LocalAccessibilityManager provides FakeAccessibilityManager(
                interactiveMillis = SPLIT_INTERACTIVE_MILLIS,
                nonInteractiveMillis = SPLIT_READING_MILLIS
            )
        )
        post(HomeMessageUiModel.StorageFull)
        post(HomeMessageUiModel.Saved)

        rule.mainClock.advanceTimeBy(19_000)
        rule.onNodeWithText(SAVED).assertExists()
        // Past the 20 s, with room for the exit.
        rule.mainClock.advanceTimeBy(3_000)
        rule.onNodeWithText(SAVED).assertDoesNotExist()
    }
}
