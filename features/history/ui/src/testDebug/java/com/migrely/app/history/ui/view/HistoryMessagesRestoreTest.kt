package com.migrely.app.history.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.requestFocus
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

private const val DELETED = "Migraine deleted"
private const val UNDO_DELETE = "Undo the deletion"
private const val SCREEN = "screen"
private const val TIME_TO_TAKE_ACTION_MILLIS = 120_000L

/**
 * History's message across rotation and process death (BRD EC-UX-57, D182): the pause ends at the save with the 3 s
 * floor, the time from the save to the restore counts, and a message past its time or its ceiling does not come back.
 * [StateRestorationTester] saves and restores at once, so the time a restore takes is added inside the restored
 * composition, before the message is restored.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class HistoryMessagesRestoreTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val presenter = HistoryMessagePresenter()
    private val clock = FakeMessageClock(wall = System.currentTimeMillis())
    private val restoration = StateRestorationTester(rule)
    private var restoreTakesMillis: Long? = null

    private fun showScreen(timeouts: FakeAccessibilityManager? = null) {
        rule.mainClock.autoAdvance = false
        restoration.setContent {
            restoreTakesMillis?.let {
                clock.advanceBy(it)
                restoreTakesMillis = null
            }
            val accessibilityManager = LocalAccessibilityManager.current
            CompositionLocalProvider(
                LocalMessageClock provides clock,
                LocalAccessibilityManager provides (timeouts ?: accessibilityManager)
            ) {
                MigrelyTheme {
                    val navController = rememberNavController()
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
                    }
                }
            }
        }
        nextFrames()
    }

    private fun nextFrames() {
        rule.mainClock.advanceTimeByFrame()
        rule.waitForIdle()
    }

    /** A deletion made now, after History opened, as History offers only those (EC-HIS-04). */
    private fun deleteNow(): Long {
        val deletedAt = clock.wall
        rule.runOnIdle { presenter.show(HistoryMessageUiModel.AttackDeleted("a", deletedAt)) }
        nextFrames()
        return deletedAt
    }

    /** Time passes on the screen: the phone's clocks and the Compose clock together. */
    private fun passTime(millis: Long) {
        clock.advanceBy(millis)
        rule.mainClock.advanceTimeBy(millis)
        rule.waitForIdle()
    }

    private fun restoreAfter(millis: Long = 0L) {
        restoreTakesMillis = millis
        // The tester disposes and re-emits the content through recompositions, which a held clock never runs. The
        // message clock alone times the countdown, so these frames add no time to it.
        rule.mainClock.autoAdvance = true
        restoration.emulateSavedInstanceStateRestore()
        rule.mainClock.autoAdvance = false
        nextFrames()
    }

    private fun holdUndo() {
        rule.onNodeWithContentDescription(UNDO_DELETE).requestFocus()
        nextFrames()
    }

    @Test
    fun `Rotate while paused gives about 3 s back, with no focus`() {
        showScreen()
        deleteNow()
        passTime(3_000)
        holdUndo()
        passTime(20_000)

        restoreAfter()

        rule.onNodeWithContentDescription(UNDO_DELETE).assertIsNotFocused()
        passTime(2_900)
        rule.onNodeWithText(DELETED).assertExists()
        passTime(200)
        rule.onNodeWithText(DELETED).assertDoesNotExist()
    }

    @Test
    fun `Process death while paused gives the time left at the save, less the wait`() {
        showScreen()
        deleteNow()
        passTime(1_000)
        holdUndo()
        passTime(60_000)

        restoreAfter(2_000)

        passTime(1_900)
        rule.onNodeWithText(DELETED).assertExists()
        passTime(200)
        rule.onNodeWithText(DELETED).assertDoesNotExist()
    }

    @Test
    fun `Restored after the time ran out shows nothing, even though it was paused`() {
        showScreen()
        deleteNow()
        passTime(1_000)
        holdUndo()

        restoreAfter(4_000)

        rule.onNodeWithText(DELETED).assertDoesNotExist()
    }

    @Test
    fun `Restored after the ceiling shows no Undo`() {
        showScreen(FakeAccessibilityManager(TIME_TO_TAKE_ACTION_MILLIS))
        deleteNow()
        rule.onNodeWithText(DELETED).assertExists()

        restoreAfter(116_000)

        rule.onNodeWithText(DELETED).assertDoesNotExist()
    }
}
