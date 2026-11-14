package com.migrely.app.home.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.home.ui.mapper.HomeMessageBehaviourUiMapper
import com.migrely.app.home.ui.mapper.SkyAppearanceUiMapper
import com.migrely.app.home.ui.model.HomeMessageUiModel
import com.migrely.app.home.ui.model.HomeViewStateUiModel
import com.migrely.app.home.ui.model.SkyUiModel
import com.migrely.app.home.ui.model.TodayUiModel
import com.migrely.app.home.ui.notification.HomeMessagePresenter
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val REVIEW = "review"
private const val HOME = "home"
private const val SETTINGS = "settings"

/**
 * The review slot as Home wires it (BRD D178 (3), EC-REV-13, ux R1–R5, review 9.2 (b)): the same `HomeWithMessages`
 * that `Home` draws, with a stand-in for `:app`'s review prompt.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class HomeReviewWiringTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val presenter = HomeMessagePresenter()
    private lateinit var navController: NavHostController

    private fun showHome() {
        rule.setContent {
            navController = rememberNavController()
            MigrelyTheme {
                NavHost(navController, startDestination = HOME) {
                    composable(HOME) {
                        HomeWithMessages(
                            navController = navController,
                            viewState = HomeViewStateUiModel.Today(TodayUiModel(SkyUiModel.Calm, 0, null)),
                            nowMillis = 0L,
                            reduceMotion = true,
                            skyAppearanceUiMapper = SkyAppearanceUiMapper(),
                            timeUiFormatter = previewFormatter,
                            actions = previewActions,
                            messageSetup = HomeMessageSetup(
                                presenter,
                                HomeMessageBehaviourUiMapper(),
                                onUndoStart = {},
                                onUndoEnd = {},
                                onUndoDelete = {}
                            ),
                            slots = HomeSlots({}, {}, {}, {}, {}, reviewPrompt = { Text(REVIEW) })
                        )
                    }
                    composable(SETTINGS) { Text(SETTINGS) }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun post(message: HomeMessageUiModel) {
        rule.runOnIdle { presenter.show(message) }
        rule.waitForIdle()
    }

    @Test
    fun `With no message the review slot is there`() {
        showHome()

        rule.onNodeWithText(REVIEW).assertExists()
    }

    @Test
    fun `While Undo shows the review slot is gone`() {
        showHome()

        post(HomeMessageUiModel.AttackEnded("a"))

        rule.onNodeWithText(REVIEW).assertDoesNotExist()
    }

    @Test
    fun `After an error is dismissed the review slot stays gone, and comes back on the next visit`() {
        showHome()
        post(HomeMessageUiModel.StorageFull)
        rule.onNodeWithContentDescription("Dismiss").performClick()
        rule.waitForIdle()
        rule.onNodeWithText(REVIEW).assertDoesNotExist()

        rule.runOnIdle { navController.navigate(SETTINGS) }
        rule.waitForIdle()
        rule.runOnIdle { navController.popBackStack() }
        rule.waitForIdle()

        rule.onNodeWithText(REVIEW).assertExists()
    }
}
