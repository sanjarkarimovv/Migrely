package com.migrely.app.medication.ui.view

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.migrely.app.architecture.ui.navigation.model.UiDestination
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.medication.domain.model.WriteResultDomainModel
import java.util.concurrent.CountDownLatch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val HOME = "home"
private const val LOG = "log"

/**
 * Undo on Log medication keeps the logged result until the intake is removed (BRD D190 (2), EC-MED-17): the page never
 * says a dose is gone while it is still in the diary.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w411dp-h891dp")
class LogMedicationUndoTest {
    @get:Rule
    val rule = createComposeRule()

    private val screen = LogMedicationHarness(rule, FakeDiary(IBUPROFEN_MEDICATION))
    private val diary get() = screen.diary

    private fun undo() = rule.onNodeWithText("Undo").performClick()

    private fun assertLoggedResultStays() {
        rule.onNode(screen.loggedLine).assertExists()
        rule.onNodeWithText("Done").assertIsEnabled()
        rule.onNodeWithText("Undo").assertIsEnabled()
        rule.onNode(screen.ibuprofen).assertDoesNotExist()
    }

    @Test
    fun `Undo that is stored shows the list and says the dose was removed`() {
        screen.show()
        screen.logIbuprofen()

        undo()
        screen.awaitText(INTAKE_UNDONE_TEXT)

        assertTrue("the intake is still stored", diary.intakes.isEmpty())
        screen.awaitNode(screen.ibuprofen)
        rule.onNode(screen.loggedLine).assertDoesNotExist()
    }

    @Test
    fun `Undo with full storage keeps the logged result while the intake is still stored`() {
        screen.show()
        screen.logIbuprofen()
        val logged = diary.intakes.toList()
        diary.deleteFailure = WriteResultDomainModel.StorageFull

        undo()
        // The removal's outcome has reached the screen once a storage message shows.
        screen.awaitNode(hasText(STORAGE_FULL_TEXT) or hasText(KEPT_STORAGE_FULL_TEXT))

        assertEquals("the intake was removed", logged, diary.intakes.toList())
        assertLoggedResultStays()
        rule.onNodeWithText(KEPT_STORAGE_FULL_TEXT).assertExists()
        rule.onNodeWithText(STORAGE_FULL_TEXT).assertDoesNotExist()
    }

    @Test
    fun `Undo that fails for another reason keeps the logged result and says it is still logged`() {
        screen.show()
        screen.logIbuprofen()
        val logged = diary.intakes.toList()
        diary.deleteFailure = WriteResultDomainModel.Failed

        undo()
        screen.awaitNode(hasText(GENERIC_TEXT) or hasText(KEPT_GENERIC_TEXT))

        assertEquals("the intake was removed", logged, diary.intakes.toList())
        assertLoggedResultStays()
        rule.onNodeWithText(KEPT_GENERIC_TEXT).assertExists()
    }

    @Test
    fun `Undo again after freeing space removes the dose and replaces the error`() {
        screen.show()
        screen.logIbuprofen()
        diary.deleteFailure = WriteResultDomainModel.StorageFull
        undo()
        screen.awaitText(KEPT_STORAGE_FULL_TEXT)
        diary.deleteFailure = null

        undo()
        screen.awaitText(INTAKE_UNDONE_TEXT)

        assertTrue("the intake is still stored", diary.intakes.isEmpty())
        rule.onNodeWithText(KEPT_STORAGE_FULL_TEXT).assertDoesNotExist()
        screen.awaitNode(screen.ibuprofen)
    }

    @Test
    fun `Two quick taps on Undo run one removal`() {
        screen.show()
        screen.logIbuprofen()
        val release = CountDownLatch(1)
        diary.holdDelete = release

        undo()
        screen.waitFor { diary.deleteCalls.isNotEmpty() }
        undo()
        release.countDown()
        screen.awaitText(INTAKE_UNDONE_TEXT)
        rule.waitForIdle()

        assertEquals(1, diary.deleteCalls.size)
    }

    // EC-MED-17 "Done before the outcome": the screen closes, the late outcome is dropped, nothing shows on return.

    @Test
    fun `Done before the outcome closes the screen, and nothing shows on return`() {
        val done = logMedicationDependencies(diary, onDone = UiDestination { it.popBackStack() })
        lateinit var navController: NavHostController
        rule.setContent {
            navController = rememberNavController()
            MigrelyTheme {
                NavHost(navController, startDestination = HOME) {
                    composable(HOME) { Text(HOME) }
                    composable(LOG) {
                        with(done) {
                            LogMedication(navController, attackId = null, darkRoom = false)
                        }
                    }
                }
            }
        }
        rule.runOnIdle { navController.navigate(LOG) }
        screen.awaitNode(screen.ibuprofen)
        screen.logIbuprofen()
        val release = CountDownLatch(1)
        diary.holdDelete = release

        undo()
        screen.waitFor { diary.deleteCalls.isNotEmpty() }
        rule.onNodeWithText("Done").performClick()
        screen.awaitText(HOME)
        release.countDown()
        screen.waitFor { diary.intakes.isEmpty() }
        rule.waitForIdle()

        rule.onNodeWithText(INTAKE_UNDONE_TEXT).assertDoesNotExist()
        rule.runOnIdle { navController.navigate(LOG) }
        screen.awaitNode(screen.ibuprofen)
        rule.waitForIdle()
        rule.onNodeWithText(INTAKE_UNDONE_TEXT).assertDoesNotExist()
        rule.onNode(screen.loggedLine).assertDoesNotExist()
    }

    @Test
    fun `While the removal runs the logged result stays`() {
        screen.show()
        screen.logIbuprofen()
        val release = CountDownLatch(1)
        diary.holdDelete = release

        undo()
        screen.waitFor { diary.deleteCalls.isNotEmpty() }
        rule.waitForIdle()

        rule.onNode(screen.loggedLine).assertExists()
        rule.onNode(screen.ibuprofen).assertDoesNotExist()
        release.countDown()
        screen.awaitText(INTAKE_UNDONE_TEXT)
    }
}
