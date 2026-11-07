package com.migrely.app.ui.main

import android.os.SystemClock
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.migrely.app.designsystem.dialog.DIALOG_WAIT_MILLIS
import com.migrely.app.designsystem.dialog.MigrelyDialog
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val CARD = "card"
private const val HOLD_MILLIS = 40L
private const val GAP_MILLIS = 400L
private const val SETTLE_MILLIS = 600L
private const val CARD_WIDTH_DP = 300
private const val CARD_HEIGHT_DP = 500
private const val FROM_THE_CARD_EDGE_DP = 24f
private const val MARGIN_DP = 10f
private const val MIN_DIRECTIONS = 3

private val DIRECTIONS = listOf(Offset(1f, 0f), Offset(-1f, 0f), Offset(0f, 1f), Offset(0f, -1f))

@Serializable
internal data object DialogLiftOnScreenRoute

/**
 * One real dialog over a real screen on a device, with real touches through the window manager, so
 * the two windows have different origins (BRD D161). Each press is stamped just after the lift, and
 * arrives once the dialog's window has gone. It presses just inside and just outside the distance,
 * in each direction, from two corners of the card, so an offset between the windows larger than the
 * margin fails one way or the other, and a wrong conversion cannot hide inside the distance.
 *
 * It needs a device and is not in the gate. It shows this AVD, in portrait. It cannot show API 26,
 * a display cutout, a dialog panned by a keyboard, or a window in split screen.
 */
class DialogLiftOnScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var open by mutableStateOf(true)
    private var cardClicks by mutableIntStateOf(0)
    private var beneathClicks by mutableIntStateOf(0)

    private val density get() = composeRule.activity.resources.displayMetrics.density
    private val slop get() = ViewConfiguration.get(composeRule.activity).scaledDoubleTapSlop

    private fun show() {
        composeRule.setContent {
            val navController = rememberNavController()
            ScreenNavHost(navController, startDestination = DialogLiftOnScreenRoute) {
                screen<DialogLiftOnScreenRoute>(navController) {
                    Box(Modifier.fillMaxSize().clickable { beneathClicks++ })
                    if (open) {
                        MigrelyDialog(onDismissRequest = { open = false }) {
                            Surface(Modifier.size(CARD_WIDTH_DP.dp, CARD_HEIGHT_DP.dp)) {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .testTag(CARD)
                                        .clickable {
                                            cardClicks++
                                            open = false
                                        }
                                )
                            }
                        }
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
    private fun settle(millis: Long = SETTLE_MILLIS) {
        Thread.sleep(millis)
        composeRule.waitForIdle()
    }

    private fun reopen() {
        composeRule.runOnUiThread { open = true }
        composeRule.waitForIdle()
        settle(DIALOG_WAIT_MILLIS + SETTLE_MILLIS)
    }

    /** The two corners of the card that are farthest apart, on the screen. */
    private fun corners(): List<Offset> {
        val card = composeRule.onNodeWithTag(CARD, useUnmergedTree = true).fetchSemanticsNode()
        val inset = FROM_THE_CARD_EDGE_DP * density
        val origin = card.positionOnScreen
        val inTheDialogsWindow = card.positionInRoot
        assertTrue(
            "the two windows must not share an origin, or this proves nothing",
            origin != inTheDialogsWindow
        )
        return listOf(
            origin + Offset(inset, inset),
            origin + Offset(card.size.width - inset, card.size.height - inset)
        )
    }

    private fun isOnScreen(point: Offset): Boolean {
        val metrics = composeRule.activity.resources.displayMetrics
        return point.x in 0f..metrics.widthPixels.toFloat() &&
            point.y in 0f..metrics.heightPixels.toFloat()
    }

    /** Lifts on the card at [lift], then presses beneath at [press]. Returns beneath's count. */
    private fun liftThenPress(lift: Offset, press: Offset): Int {
        val before = beneathClicks
        val cards = cardClicks
        val liftedAt = tap(SystemClock.uptimeMillis(), lift)
        composeRule.runOnIdle { assertEquals("the card answers at $lift", cards + 1, cardClicks) }
        settle()
        composeRule.onNodeWithTag(CARD, useUnmergedTree = true).assertDoesNotExist()
        tap(liftedAt + GAP_MILLIS, press)
        return composeRule.runOnIdle { beneathClicks - before }
    }

    @Test
    fun aPressAtTheLiftsPointDoesNothingAndAfterTheLimitItAnswers() {
        show()
        settle(DIALOG_WAIT_MILLIS + SETTLE_MILLIS)
        val point = corners().first()

        assertEquals("the second tap", 0, liftThenPress(point, point))

        settle()
        tap(SystemClock.uptimeMillis(), point)
        composeRule.runOnIdle { assertEquals("a tap after the limit", 1, beneathClicks) }
    }

    @Test
    fun justInsideTheDistanceDoesNothingAndJustOutsideItAnswers() {
        show()
        settle(DIALOG_WAIT_MILLIS + SETTLE_MILLIS)
        val inside = slop - MARGIN_DP * density
        val outside = slop + MARGIN_DP * density

        corners().forEach { corner ->
            val directions = DIRECTIONS.filter { isOnScreen(corner + it * outside) }
            assertTrue("$corner has few directions", directions.size >= MIN_DIRECTIONS)
            directions.forEach { direction ->
                if (!open) reopen()
                assertEquals(
                    "$inside px from $corner towards $direction",
                    0,
                    liftThenPress(corner, corner + direction * inside)
                )
                settle()
                reopen()
                assertEquals(
                    "$outside px from $corner towards $direction",
                    1,
                    liftThenPress(corner, corner + direction * outside)
                )
                settle()
            }
        }
    }
}
