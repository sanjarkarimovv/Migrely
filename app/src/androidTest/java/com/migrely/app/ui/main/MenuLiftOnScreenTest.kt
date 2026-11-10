package com.migrely.app.ui.main

import android.os.SystemClock
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import com.migrely.app.designsystem.dialog.MigrelyDropdownMenu
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private const val ITEM = "item"
private const val HOLD_MILLIS = 40L
private const val GAP_MILLIS = 400L
private const val SETTLE_MILLIS = 600L
private const val FROM_THE_ITEM_EDGE_DP = 24f
private const val MARGIN_DP = 10f
private const val MIN_DIRECTIONS = 3

private val DIRECTIONS = listOf(Offset(1f, 0f), Offset(-1f, 0f), Offset(0f, 1f), Offset(0f, -1f))

@Serializable
internal data object MenuLiftOnScreenRoute

/**
 * One real popup menu over a real screen on a device, with real touches through the window
 * manager, so the popup's window and the screen's have different origins (issue #161, BRD D164).
 * Each press is stamped just after the lift on the item, and arrives once the menu's window has
 * gone. It presses just inside and just outside the distance, in each direction, from both ends of
 * the item, so an offset between the windows larger than the margin fails one way or the other.
 *
 * It needs a device and is not in the gate. It shows this AVD, in portrait. It cannot show API 26,
 * a display cutout, or a window in split screen.
 */
class MenuLiftOnScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var expanded by mutableStateOf(false)
    private var itemClicks by mutableIntStateOf(0)
    private var beneathClicks by mutableIntStateOf(0)

    private val density get() = composeRule.activity.resources.displayMetrics.density
    private val slop get() = ViewConfiguration.get(composeRule.activity).scaledDoubleTapSlop

    private fun show() {
        composeRule.setContent {
            val navController = rememberNavController()
            ScreenNavHost(navController, startDestination = MenuLiftOnScreenRoute) {
                screen<MenuLiftOnScreenRoute>(navController) {
                    Box(Modifier.fillMaxSize().clickable { beneathClicks++ })
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Box {
                            Text("Tag")
                            MigrelyDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Remove from my list") },
                                    onClick = {
                                        itemClicks++
                                        expanded = false
                                    },
                                    modifier = Modifier.testTag(ITEM)
                                )
                            }
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()
        settle()
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

    /** Real time, because the menu's window goes in real time. */
    private fun settle() {
        Thread.sleep(SETTLE_MILLIS)
        composeRule.waitForIdle()
    }

    private fun open() {
        composeRule.runOnUiThread { expanded = true }
        composeRule.waitForIdle()
        settle()
    }

    /** Both ends of the item, on the screen. */
    private fun ends(): List<Offset> {
        val item = composeRule.onNodeWithTag(ITEM, useUnmergedTree = true).fetchSemanticsNode()
        val inset = FROM_THE_ITEM_EDGE_DP * density
        val origin = item.positionOnScreen
        assertTrue(
            "the two windows must not share an origin, or this proves nothing",
            origin != item.positionInRoot
        )
        val middle = item.size.height / 2f
        return listOf(
            origin + Offset(inset, middle),
            origin + Offset(item.size.width - inset, middle)
        )
    }

    private fun isOnScreen(point: Offset): Boolean {
        val metrics = composeRule.activity.resources.displayMetrics
        return point.x in 0f..metrics.widthPixels.toFloat() &&
            point.y in 0f..metrics.heightPixels.toFloat()
    }

    /** Opens the menu, lifts on its item at [lift], then presses beneath at [press]. */
    private fun liftThenPress(lift: Offset, press: Offset): Int {
        open()
        val before = beneathClicks
        val items = itemClicks
        val liftedAt = tap(SystemClock.uptimeMillis(), lift)
        composeRule.runOnIdle { assertEquals("the item answers at $lift", items + 1, itemClicks) }
        settle()
        composeRule.onNodeWithTag(ITEM, useUnmergedTree = true).assertDoesNotExist()
        tap(liftedAt + GAP_MILLIS, press)
        val beneath = composeRule.runOnIdle { beneathClicks - before }
        settle()
        return beneath
    }

    @Test
    fun aPressAtTheLiftsPointDoesNothingAndAfterTheLimitItAnswers() {
        show()
        open()
        val point = ends().first()
        composeRule.runOnUiThread { expanded = false }
        settle()

        assertEquals("the second tap", 0, liftThenPress(point, point))

        tap(SystemClock.uptimeMillis(), point)
        composeRule.runOnIdle { assertEquals("a tap after the limit", 1, beneathClicks) }
    }

    @Test
    fun justInsideTheDistanceDoesNothingAndJustOutsideItAnswers() {
        show()
        open()
        val ends = ends()
        composeRule.runOnUiThread { expanded = false }
        settle()
        val inside = slop - MARGIN_DP * density
        val outside = slop + MARGIN_DP * density

        ends.forEach { end ->
            val directions = DIRECTIONS.filter { isOnScreen(end + it * outside) }
            assertTrue("$end has few directions", directions.size >= MIN_DIRECTIONS)
            directions.forEach { direction ->
                assertEquals(
                    "$inside px from $end towards $direction",
                    0,
                    liftThenPress(end, end + direction * inside)
                )
                assertEquals(
                    "$outside px from $end towards $direction",
                    1,
                    liftThenPress(end, end + direction * outside)
                )
            }
        }
    }
}
