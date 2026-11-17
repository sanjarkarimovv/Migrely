package com.migrely.app.home.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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

    /** Home with its messages and its review slot, inside a NavHost with one other screen. */
    private fun showHome() {
        rule.setContent {
            navController = rememberNavController()
            MigrelyTheme {
                NavHost(navController, startDestination = HOME) {
                    composable(HOME) { HomeWithReview() }
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
            onUndoDelete = {},
            onMessageBottomChanged = {},
            onShownChanged = gate::onShownChanged,
            onErrorPosted = gate::onErrorPosted
        )
    }

    private fun post(message: HomeMessageUiModel) {
        rule.runOnIdle { presenter.show(message) }
        rule.waitForIdle()
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
}
