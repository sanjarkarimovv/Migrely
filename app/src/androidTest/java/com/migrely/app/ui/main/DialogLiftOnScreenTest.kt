package com.migrely.app.ui.main

import android.os.SystemClock
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.migrely.app.designsystem.dialog.DIALOG_WAIT_MILLIS
import com.migrely.app.designsystem.dialog.MigrelyAlertDialog
import kotlin.math.abs
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val OPTION = "option"
private const val BENEATH = "beneath"
private const val HOLD_MILLIS = 40L
private const val GAP_MILLIS = 100L
private const val SETTLE_MILLIS = 600L

@Serializable
internal data object DialogLiftOnScreenRoute

/**
 * One real dialog over a real screen on a device, with real touches through the window manager, so
 * the two windows have different origins (BRD D161). The press is stamped just after the lift, and
 * arrives once the dialog's window has gone. A wrong conversion to the screen would fail toward
 * answering, which this catches.
 */
class DialogLiftOnScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var open by mutableStateOf(true)
    private var optionClicks by mutableIntStateOf(0)
    private var beneathClicks by mutableIntStateOf(0)

    private fun show() {
        composeRule.setContent {
            val navController = rememberNavController()
            ScreenNavHost(navController, startDestination = DialogLiftOnScreenRoute) {
                screen<DialogLiftOnScreenRoute>(navController) {
                    Box(Modifier.fillMaxSize().testTag(BENEATH).clickable { beneathClicks++ })
                    if (open) {
                        MigrelyAlertDialog(
                            onDismissRequest = { open = false },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        optionClicks++
                                        open = false
                                    },
                                    modifier = Modifier.testTag(OPTION)
                                ) {
                                    Text("Option")
                                }
                            }
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun send(action: Int, downAt: Long, at: Long, point: Offset) {
        val event = MotionEvent.obtain(downAt, at, action, point.x, point.y, 0)
        InstrumentationRegistry.getInstrumentation().sendPointerSync(event)
        event.recycle()
    }

    /** Returns when it lifted. */
    private fun tap(pressedAt: Long, point: Offset): Long {
        val liftedAt = pressedAt + HOLD_MILLIS
        send(MotionEvent.ACTION_DOWN, pressedAt, pressedAt, point)
        send(MotionEvent.ACTION_UP, pressedAt, liftedAt, point)
        composeRule.waitForIdle()
        return liftedAt
    }

    /** Real time, because the dialog's window goes in real time and the wait reads uptime. */
    private fun settle() {
        Thread.sleep(SETTLE_MILLIS)
        composeRule.waitForIdle()
    }

    @Test
    fun aPressAtTheOptionsPointDoesNothingAndAfterTheTimeoutItAnswers() {
        show()
        Thread.sleep(DIALOG_WAIT_MILLIS + SETTLE_MILLIS)
        composeRule.waitForIdle()
        val option = composeRule.onNodeWithTag(OPTION, useUnmergedTree = true).fetchSemanticsNode()
        val centre = Offset(option.size.width / 2f, option.size.height / 2f)
        val onScreen = option.positionOnScreen + centre
        val inTheDialogsWindow = option.positionInRoot + centre
        assertTrue(
            "the two windows must not share an origin, or this proves nothing",
            abs(onScreen.y - inTheDialogsWindow.y) > 0f
        )

        val liftedAt = tap(SystemClock.uptimeMillis(), onScreen)
        composeRule.runOnIdle { assertEquals("the option answers", 1, optionClicks) }
        settle()
        composeRule.onNodeWithTag(OPTION, useUnmergedTree = true).assertDoesNotExist()

        tap(liftedAt + GAP_MILLIS, onScreen)
        composeRule.runOnIdle { assertEquals("the second tap", 0, beneathClicks) }

        settle()
        tap(SystemClock.uptimeMillis(), onScreen)
        composeRule.runOnIdle { assertEquals("a tap after the timeout", 1, beneathClicks) }
    }
}
