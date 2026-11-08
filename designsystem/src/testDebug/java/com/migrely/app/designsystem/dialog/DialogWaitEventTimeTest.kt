package com.migrely.app.designsystem.dialog

import android.os.Looper
import android.os.SystemClock
import android.view.MotionEvent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import java.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog

private const val BUTTON = "button"
private const val IN_THE_WAIT = 300L
private const val AFTER_THE_WAIT = 800L
private const val JUST_INSIDE = 1L

/**
 * What the wait reads: the time on the press's own event, which only a hand-built event can set
 * apart from the clock, because a Compose test's injected touch is always stamped with the clock's
 * time.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h800dp")
class DialogWaitEventTimeTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var clicks by mutableIntStateOf(0)

    @Composable
    private fun TheDialog() {
        MigrelyAlertDialog(
            onDismissRequest = {},
            confirmButton = {
                TextButton(onClick = { clicks++ }, modifier = Modifier.testTag(BUTTON)) {
                    Text("Do")
                }
            }
        )
    }

    /** A press and its lift, stamped [pressedAtMillis], whatever the time is when they arrive. */
    private fun tapStamped(pressedAtMillis: Long) {
        val centre = composeRule.onNodeWithTag(BUTTON, useUnmergedTree = true)
            .fetchSemanticsNode().boundsInWindow.center
        deliver(MotionEvent.ACTION_DOWN, pressedAtMillis, centre.x, centre.y)
        deliver(MotionEvent.ACTION_UP, pressedAtMillis, centre.x, centre.y)
    }

    private fun deliver(action: Int, atMillis: Long, x: Float, y: Float) {
        composeRule.runOnUiThread {
            val event = MotionEvent.obtain(atMillis, atMillis, action, x, y, 0)
            ShadowDialog.getLatestDialog().dispatchTouchEvent(event)
            event.recycle()
        }
        composeRule.waitForIdle()
    }

    private fun assertClicks(expected: Int) = composeRule.runOnIdle {
        assertEquals(expected, clicks)
    }

    /** A slow frame: the press happened in the wait, and is only delivered long after it ended. */
    @Test
    fun aPressStampedInTheWaitAndDeliveredAfterItIsIgnored() {
        val harness = DialogWaitHarness(composeRule)
        harness.setUp()
        harness.show { TheDialog() }
        harness.goTo(AFTER_THE_WAIT)

        tapStamped(harness.appearedAt() + IN_THE_WAIT)
        assertClicks(0)

        tapStamped(harness.appearedAt() + AFTER_THE_WAIT)
        assertClicks(1)
    }

    /**
     * No clock provided, as in the app. The wall clock would not do: a press is stamped with
     * uptime, so every press would be ignored for good.
     */
    @Test
    fun theDefaultClockIsTheOneTouchEventsUse() {
        // Far from zero, as on a phone: Robolectric's uptime starts so low that a clock stuck at
        // zero would still see the first press as inside the wait.
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofHours(1))
        val before = SystemClock.uptimeMillis()
        composeRule.setContent { MaterialTheme { TheDialog() } }
        composeRule.waitForIdle()
        val after = SystemClock.uptimeMillis()
        assertTrue(
            "the dialog took ${after - before} ms to appear, past the press called inside",
            after - before + JUST_INSIDE < DIALOG_WAIT_MILLIS
        )

        tapStamped(after + JUST_INSIDE)
        assertClicks(0)

        tapStamped(after + DIALOG_WAIT_MILLIS)
        assertClicks(1)
    }
}
