package com.migrely.app.medication.ui.view

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
import com.migrely.app.medication.ui.mapper.MedicationMessageBehaviourUiMapper
import com.migrely.app.medication.ui.model.MedicationEventUiModel
import com.migrely.app.medication.ui.model.MedicationMessageUiModel
import com.migrely.app.medication.ui.notification.MedicationEventPresenter
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val STORAGE_FULL = "Couldn't save. Your phone's storage is full."
private const val SCREEN = "screen"
private const val OTHER = "other"

/** The medication screens after D178: an error belongs to the visit, and their own message replaces it (BRD EC-UX-35). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MedicationMessagesTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val presenter = MedicationEventPresenter()
    private lateinit var navController: NavHostController

    private fun showScreen() {
        rule.setContent {
            navController = rememberNavController()
            MigrelyTheme {
                NavHost(navController, startDestination = SCREEN) {
                    composable(SCREEN) {
                        MedicationMessages(
                            presenter = presenter,
                            navController = navController,
                            behaviourMapper = MedicationMessageBehaviourUiMapper(),
                            reduceMotion = true
                        )
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
}
