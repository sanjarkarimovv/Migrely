package com.migrely.app.designsystem.snackbar

import android.app.UiAutomation
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val UNDO = "Undo the start"
private const val CONTENT = "Content"
private const val TIMEOUT_MILLIS = 4_000L
private const val LEFT_BEFORE_FOCUS_MILLIS = 1_500L
private const val GRACE_MILLIS = 300L
private const val FLOOR_MILLIS = 3_000L
private const val ROUNDS = 10
private const val WAIT_MILLIS = 2_000L
private const val POLL_MILLIS = 10L

// What a resume may have lost by the time it is read: the poll, the frame, the device.
private const val RESUME_SLACK_MILLIS = 400L
private const val TAG = "T64Pause"

/**
 * Task 7.2 (BRD D182 (2), design D3, cr-t64 S3 #2): the production TalkBack path, the real [MigrelySnackbarHost] with
 * [ScreenReaderFocusEffect] feeding [rememberMessageCountdown], with TalkBack on. Each round shows a fresh message, waits
 * until it has [LEFT_BEFORE_FOCUS_MILLIS] left, moves TalkBack's focus to its text, then to its control well within the
 * grace, then off to the content, and checks:
 * - paused on the text;
 * - no resume during the move to the control;
 * - still paused just after focus leaves, resumed once the grace has passed, with at least the 3 s floor left.
 *
 * UiAutomation moves the focus, as a TalkBack swipe does, without suppressing TalkBack. With TalkBack off it skips
 * itself, because Compose sends no focus events without an enabled service and the run would prove nothing.
 */
@RunWith(AndroidJUnit4::class)
class MessageTalkBackPauseTest {
    @get:Rule
    val rule = createComposeRule()

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val automation: UiAutomation =
        instrumentation.getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)
    private var shown by mutableStateOf<String?>(null)
    private var countdown: MessageCountdownState? = null

    @Before
    fun talkBackIsOn() {
        val manager = instrumentation.targetContext
            .getSystemService(AccessibilityManager::class.java)
        assumeTrue(
            "TalkBack is off: this test needs TalkBack on, and a run without it proves nothing",
            manager.isEnabled && manager.isTouchExplorationEnabled
        )
    }

    @Test
    fun talkBackOnTheTextThenTheControlHoldsTheMessageAndLeavingResumesItAfterTheGrace() {
        show()

        val kept = (1..ROUNDS).count { round -> round(round) }

        assertEquals(ROUNDS, kept)
    }

    private fun show() {
        rule.setContent {
            MigrelyTheme {
                val state = rememberMessageCountdown(
                    message = shown,
                    timeoutMillis = TIMEOUT_MILLIS,
                    undoActionWallMillis = System.currentTimeMillis(),
                    onExpired = { shown = null }
                )
                countdown = state
                Column {
                    Button(onClick = {}) { Text(CONTENT) }
                    MigrelySnackbarHost(
                        message = shown?.takeUnless { state.isOver },
                        reduceMotion = true,
                        countdown = state
                    ) { text ->
                        MigrelySnackbar(
                            text = text,
                            tone = SnackbarTone.Confirmation,
                            action = SnackbarAction("Undo", UNDO) {},
                            baseDurationMillis = TIMEOUT_MILLIS,
                            dismissLabel = "Dismiss",
                            onDismiss = {}
                        )
                    }
                }
            }
        }
        rule.waitForIdle()
    }

    private fun round(round: Int): Boolean {
        val text = "Migraine started $round"
        rule.runOnIdle { shown = text }
        rule.waitForIdle()
        SystemClock.sleep(TIMEOUT_MILLIS - LEFT_BEFORE_FOCUS_MILLIS)

        val pausedOnText = moveFocus(text) && await { paused() == true }
        val keptThroughMove =
            pausedOnText && moveFocus(UNDO) && stays(GRACE_MILLIS * 2) { paused() == true }
        val leftAt = SystemClock.uptimeMillis()
        val heldInGrace = keptThroughMove && moveFocus(CONTENT) && paused() == true
        val checkedAfterMillis = SystemClock.uptimeMillis() - leftAt
        val resumed = heldInGrace && await { paused() == false }
        val left = rule.runOnUiThread<Long> { countdown?.countdown?.remainingMillis() ?: 0L }
        val floored = resumed && left >= FLOOR_MILLIS - RESUME_SLACK_MILLIS
        Log.i(
            TAG,
            "round $round: pausedOnText=$pausedOnText keptThroughMove=$keptThroughMove " +
                "heldInGrace=$heldInGrace (checked ${checkedAfterMillis}ms after leaving) resumed=$resumed leftMillis=$left"
        )
        return floored
    }

    /** Read on the UI thread, where the countdown lives. */
    private fun paused(): Boolean? = rule.runOnUiThread<Boolean?> {
        countdown?.countdown?.isPaused()
    }

    /**
     * Moves TalkBack's focus to [label]. UiAutomation's node cache went stale between rounds and missed the new message
     * in 5 of 10 rounds, so the cache is cleared and the node refreshed first. Whether TalkBack had already put its
     * focus there by itself is logged: it did on its own in about half the rounds (task 7.2 result).
     */
    private fun moveFocus(label: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) automation.clearCache()
        val node = automation.rootInActiveWindow.findByLabel(label)
        if (node == null) {
            val labels = labels(automation.rootInActiveWindow)
            Log.i(TAG, "move to '$label': not found; labels=$labels")
            return false
        }
        node.refresh()
        val alreadyFocused = node.isAccessibilityFocused
        node.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS)
        Log.i(TAG, "move to '$label': TalkBack focus already there=$alreadyFocused")
        return true
    }

    private fun labels(node: AccessibilityNodeInfo?): List<String> {
        if (node == null) return emptyList()
        val own = listOfNotNull(node.text?.toString(), node.contentDescription?.toString())
        return own + (0 until node.childCount).flatMap { labels(node.getChild(it)) }
    }

    private fun await(condition: () -> Boolean): Boolean {
        val end = SystemClock.uptimeMillis() + WAIT_MILLIS
        while (SystemClock.uptimeMillis() < end) {
            if (condition()) return true
            SystemClock.sleep(POLL_MILLIS)
        }
        return condition()
    }

    /** Whether [condition] holds at every poll for [millis]. */
    private fun stays(millis: Long, condition: () -> Boolean): Boolean {
        val end = SystemClock.uptimeMillis() + millis
        while (SystemClock.uptimeMillis() < end) {
            if (!condition()) return false
            SystemClock.sleep(POLL_MILLIS)
        }
        return true
    }
}
