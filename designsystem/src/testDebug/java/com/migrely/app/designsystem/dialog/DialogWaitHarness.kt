package com.migrely.app.designsystem.dialog

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToString
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

private const val FRAMES_TO_APPEAR = 20

/**
 * Drives a dialog in a real window on the test's clock. A wrapper's own clock reads that one too,
 * and the moment it read is kept: every gap a test names is measured from it, so it is exactly the
 * gap the node judges. A dialog's content composes some frames after the state that shows it.
 */
internal class DialogWaitHarness(private val composeRule: ComposeContentTestRule) {
    private var appearedAtMillis: Long? = null
    private var content by mutableStateOf<(@Composable () -> Unit)?>(null)

    fun setUp() {
        composeRule.setContent {
            CompositionLocalProvider(
                LocalDialogClock provides {
                    composeRule.mainClock.currentTime.also { appearedAtMillis = it }
                },
                LocalDialogLiftRule provides DialogLiftRule(
                    LocalViewConfiguration.current.doubleTapTimeoutMillis
                )
            ) {
                MaterialTheme { content?.invoke() }
            }
        }
        composeRule.waitForIdle()
        composeRule.mainClock.autoAdvance = false
    }

    /**
     * Shows the dialog and stops at the first frame that has it. A dialog that reads no clock, a
     * raw one, is measured from that frame.
     */
    fun show(dialog: @Composable () -> Unit) {
        appearedAtMillis = null
        composeRule.runOnUiThread { content = dialog }
        var frames = 0
        while (!isUp() && frames++ < FRAMES_TO_APPEAR) {
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.waitForIdle()
        }
        assertTrue("the dialog did not appear", isUp())
        if (appearedAtMillis == null) appearedAtMillis = composeRule.mainClock.currentTime
    }

    private fun isUp() = composeRule.onAllNodes(isDialog(), useUnmergedTree = true)
        .fetchSemanticsNodes().isNotEmpty()

    /** Takes the dialog away and checks it went, or the next [show] would find this one up. */
    fun hide() {
        composeRule.runOnUiThread { content = {} }
        assertGone()
    }

    fun assertGone() {
        var frames = 0
        while (isUp() && frames++ < FRAMES_TO_APPEAR) {
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.waitForIdle()
        }
        assertFalse("the dialog did not go", isUp())
    }

    fun appearedAt(): Long = checkNotNull(appearedAtMillis)

    /** Moves the clock to exactly [millis] after the dialog appeared. */
    fun goTo(millis: Long) {
        val left = checkNotNull(appearedAtMillis) + millis - composeRule.mainClock.currentTime
        assertTrue("already $left ms past the point this test names", left >= 0)
        composeRule.mainClock.advanceTimeBy(left, ignoreFrameDuration = true)
        composeRule.waitForIdle()
    }

    fun tap(node: SemanticsNodeInteraction) {
        node.performTouchInput {
            down(center)
            up()
        }
        composeRule.waitForIdle()
    }

    fun tagged(tag: String) = composeRule.onNodeWithTag(tag, useUnmergedTree = true)

    fun matching(matcher: SemanticsMatcher) = composeRule.onNode(matcher, useUnmergedTree = true)

    /**
     * The dialog's whole tree with its bounds, without the node ids that differ between two runs.
     */
    fun dialogTree() = composeRule.onNode(isDialog(), useUnmergedTree = true)
        .printToString(Int.MAX_VALUE)
        .replace(Regex("Node #\\d+"), "Node")
}
