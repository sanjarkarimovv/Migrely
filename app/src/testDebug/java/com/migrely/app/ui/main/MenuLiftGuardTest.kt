package com.migrely.app.ui.main

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.navigation.compose.rememberNavController
import com.migrely.app.designsystem.dialog.DIALOG_WAIT_MILLIS
import com.migrely.app.designsystem.dialog.MigrelyDropdownMenu
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val PAGE = "page"
private const val ITEM = "item"
private const val FADE_OVER = 400L
private const val OPEN_ANIMATION_OVER = 300L
private const val UNDER_THE_LIMIT = 250L
private const val FRAMES_TO_APPEAR = 20

@Serializable
internal data object MenuLiftRoute

/**
 * The real `ScreenNavHost` and `screen()`, with a real `MigrelyDropdownMenu` over the screen: a
 * lift on the menu's item counts for the main window, as a dialog's does (issue #161, BRD D164).
 * The menu has no wait, so the clock that is internal to `designsystem` does not matter here.
 * Robolectric's windows share an origin; `MenuLiftOnScreenTest` checks them on a device.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w400dp-h800dp")
class MenuLiftGuardTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var expanded by mutableStateOf(false)
    private var pageClicks by mutableIntStateOf(0)
    private var itemClicks by mutableIntStateOf(0)

    @Before
    fun setUp() {
        composeRule.setContent {
            val navController = rememberNavController()
            ScreenNavHost(navController, startDestination = MenuLiftRoute) {
                screen<MenuLiftRoute>(navController) {
                    Box(Modifier.fillMaxSize().testTag(PAGE).clickable { pageClicks++ })
                    Box {
                        Text("Tag")
                        MigrelyDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Remove") },
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
        composeRule.waitForIdle()
        composeRule.mainClock.autoAdvance = false
    }

    private fun tagged(tag: String) = composeRule.onNodeWithTag(tag, useUnmergedTree = true)

    private fun isMenuUp() = composeRule.onAllNodes(isPopup(), useUnmergedTree = true)
        .fetchSemanticsNodes().isNotEmpty()

    private fun after(millis: Long) {
        composeRule.mainClock.advanceTimeBy(millis, ignoreFrameDuration = true)
        composeRule.waitForIdle()
    }

    private fun SemanticsNodeInteraction.centreOnScreen(): Offset {
        val node = fetchSemanticsNode()
        return node.positionOnScreen + Offset(node.size.width / 2f, node.size.height / 2f)
    }

    private fun SemanticsNodeInteraction.tapAt(inside: Offset) {
        performTouchInput {
            down(inside)
            up()
        }
        composeRule.waitForIdle()
    }

    private fun SemanticsNodeInteraction.tapAtCentre() {
        performTouchInput {
            down(center)
            up()
        }
        composeRule.waitForIdle()
    }

    /** Touches the screen once, opens the menu and taps its item. Returns where and when. */
    private fun removeThroughTheMenu(): Pair<Offset, Long> {
        tagged(PAGE).tapAtCentre()
        after(FADE_OVER)
        assertEquals("the screen answers before the menu", 1, pageClicks)
        pageClicks = 0

        composeRule.runOnUiThread { expanded = true }
        var frames = 0
        while (!isMenuUp() && frames++ < FRAMES_TO_APPEAR) {
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.waitForIdle()
        }
        assertTrue("the menu did not appear", isMenuUp())
        after(OPEN_ANIMATION_OVER)

        val point = tagged(ITEM).centreOnScreen()
        tagged(ITEM).tapAtCentre()
        composeRule.runOnIdle { assertEquals("the item answers", 1, itemClicks) }
        return point to composeRule.mainClock.currentTime
    }

    private fun pressThePageAfter(liftedAt: Long, millis: Long, at: Offset) {
        after(liftedAt + millis - composeRule.mainClock.currentTime)
        assertFalse("the menu is still up", isMenuUp())
        tagged(PAGE).tapAt(at - tagged(PAGE).fetchSemanticsNode().positionOnScreen)
    }

    @Test
    fun aPressOnTheScreenUnderTheLimitDoesNothing() {
        val (point, liftedAt) = removeThroughTheMenu()

        pressThePageAfter(liftedAt, UNDER_THE_LIMIT, point)

        composeRule.runOnIdle { assertEquals(0, pageClicks) }
    }

    @Test
    fun atTheLimitItAnswers() {
        val (point, liftedAt) = removeThroughTheMenu()

        pressThePageAfter(liftedAt, DIALOG_WAIT_MILLIS, point)

        composeRule.runOnIdle { assertEquals(1, pageClicks) }
    }
}
