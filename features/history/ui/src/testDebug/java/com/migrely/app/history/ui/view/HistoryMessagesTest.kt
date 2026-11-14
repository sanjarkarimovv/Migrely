package com.migrely.app.history.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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

/** History after D178: "Saved." replaces an error and nothing else, and an error belongs to the visit (BRD EC-UX-35). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HistoryMessagesTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val presenter = HistoryMessagePresenter()
    private lateinit var navController: NavHostController

    private fun showScreen() {
        rule.setContent {
            navController = rememberNavController()
            MigrelyTheme {
                NavHost(navController, startDestination = SCREEN) {
                    composable(SCREEN) {
                        HistoryMessages(
                            presenter = presenter,
                            navController = navController,
                            behaviourMapper = HistoryMessageBehaviourUiMapper(),
                            reduceMotion = true,
                            onUndoDelete = {}
                        )
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
}
