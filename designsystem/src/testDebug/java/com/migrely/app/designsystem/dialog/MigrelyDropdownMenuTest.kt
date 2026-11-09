package com.migrely.app.designsystem.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val BENEATH = "beneath"
private const val ITEM = "item"
private const val REMOVE = "Remove"
private const val PAST_THE_WAIT = 800L
private const val OPEN_ANIMATION_OVER = 300L

/** The menu's window takes some frames to go, so beneath is reached from here. */
private const val UNDER_THE_LIMIT = 250L

private const val JUST_UNDER_THE_LIMIT = DIALOG_WAIT_MILLIS - 1L
private const val UNDER_THE_BURST_LIMIT = 250L
private const val PAST_THE_BURST_LIMIT = 350L
private const val BEYOND_DP_AT_MDPI = 350f
private const val FRAMES_TO_APPEAR = 20

private typealias Menu = @Composable (content: @Composable ColumnScope.() -> Unit) -> Unit

/**
 * A popup menu's item over a control in another window, in one test's windows: a lift on the
 * menu's card counts for what is beneath it, as a lift on a dialog's card does, and the menu has no
 * wait (issue #161, BRD D164). The window beneath is a full-screen dialog, because the main
 * window's side lives in `app`. Robolectric's windows share an origin, so that the popup's window
 * and the one beneath agree is proved on a device, by `MenuLiftOnScreenTest` in `app`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h800dp")
class MigrelyDropdownMenuTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val harness = DialogWaitHarness(composeRule)
    private var expanded by mutableStateOf(false)
    private var anchorShown by mutableStateOf(true)
    private var itemClicks by mutableIntStateOf(0)
    private var beneathClicks by mutableIntStateOf(0)
    private var itemRemovesTheAnchor = false

    @Before
    fun setUp() = harness.setUp()

    private val throughTheHelper: Menu = { content ->
        MigrelyDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            content()
        }
    }

    private val raw: Menu = { content ->
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) { content() }
    }

    private fun showOverAnOlderDialog(menu: Menu = throughTheHelper) {
        harness.show {
            MigrelyDialog(onDismissRequest = {}, fullScreen = true) {
                Surface {
                    Box(Modifier.fillMaxSize().testTag(BENEATH).clickable { beneathClicks++ })
                    if (anchorShown) {
                        Box {
                            Text("Tag")
                            menu {
                                DropdownMenuItem(
                                    text = { Text(REMOVE) },
                                    onClick = {
                                        itemClicks++
                                        expanded = false
                                        if (itemRemovesTheAnchor) anchorShown = false
                                    },
                                    modifier = Modifier.testTag(ITEM)
                                )
                            }
                        }
                    }
                }
            }
        }
        harness.goTo(PAST_THE_WAIT)
    }

    private fun isMenuUp() = composeRule.onAllNodes(isPopup(), useUnmergedTree = true)
        .fetchSemanticsNodes().isNotEmpty()

    /** Opens the menu and stops at the first frame that has it. */
    private fun openMenu() {
        composeRule.runOnUiThread { expanded = true }
        var frames = 0
        while (!isMenuUp() && frames++ < FRAMES_TO_APPEAR) {
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.waitForIdle()
        }
        assertTrue("the menu did not appear", isMenuUp())
    }

    private fun openMenuAndLetItSettle() {
        openMenu()
        after(OPEN_ANIMATION_OVER)
    }

    private fun after(millis: Long) {
        composeRule.mainClock.advanceTimeBy(millis, ignoreFrameDuration = true)
        composeRule.waitForIdle()
    }

    private fun goToAfter(from: Long, millis: Long) =
        after(from + millis - composeRule.mainClock.currentTime)

    /** As [goToAfter], where the menu's window has gone, so a press there is beneath. */
    private fun goBeneathAfter(liftedAt: Long, millis: Long) {
        goToAfter(liftedAt, millis)
        assertFalse("the menu is still up", isMenuUp())
    }

    private fun item() = harness.tagged(ITEM)

    private fun beneath() = harness.tagged(BENEATH)

    private fun SemanticsNodeInteraction.centreOnScreen(): Offset {
        val node = fetchSemanticsNode()
        return node.positionOnScreen + Offset(node.size.width / 2f, node.size.height / 2f)
    }

    /** Taps the item at its centre. Returns that point on the screen and when the touch lifted. */
    private fun tapItem(): Pair<Offset, Long> {
        val point = item().centreOnScreen()
        harness.tap(item())
        return point to composeRule.mainClock.currentTime
    }

    /** Taps the item of a settled menu, and lets the menu go. */
    private fun removeThroughTheMenu(): Pair<Offset, Long> {
        showOverAnOlderDialog()
        openMenuAndLetItSettle()
        val tap = tapItem()
        composeRule.runOnIdle { assertEquals("the item answers", 1, itemClicks) }
        return tap
    }

    private fun assertMenuGone() {
        var frames = 0
        while (isMenuUp() && frames++ < FRAMES_TO_APPEAR) {
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.waitForIdle()
        }
        assertFalse("the menu did not go", isMenuUp())
    }

    /** A press and lift beneath at this point of the screen. Returns when it lifted. */
    private fun pressBeneathAt(onScreen: Offset): Long {
        val inside = onScreen - beneath().fetchSemanticsNode().positionOnScreen
        beneath().performTouchInput {
            down(inside)
            up()
        }
        composeRule.waitForIdle()
        return composeRule.mainClock.currentTime
    }

    private fun assertBeneathClicks(expected: Int) = composeRule.runOnIdle {
        assertEquals(expected, beneathClicks)
    }

    @Test
    fun aPressBeneathUnderTheLimitDoesNothing() {
        val (point, liftedAt) = removeThroughTheMenu()

        goBeneathAfter(liftedAt, UNDER_THE_LIMIT)
        pressBeneathAt(point)

        assertBeneathClicks(0)
    }

    @Test
    fun aPressBeneathJustUnderTheLimitDoesNothing() {
        val (point, liftedAt) = removeThroughTheMenu()

        goBeneathAfter(liftedAt, JUST_UNDER_THE_LIMIT)
        pressBeneathAt(point)

        assertBeneathClicks(0)
    }

    @Test
    fun aPressBeneathAtTheLimitAnswers() {
        val (point, liftedAt) = removeThroughTheMenu()

        goBeneathAfter(liftedAt, DIALOG_WAIT_MILLIS)
        pressBeneathAt(point)

        assertBeneathClicks(1)
    }

    @Test
    fun aPressBeneathBeyondTheDistanceAnswers() {
        val (point, liftedAt) = removeThroughTheMenu()

        goBeneathAfter(liftedAt, UNDER_THE_LIMIT)
        pressBeneathAt(point + Offset(0f, BEYOND_DP_AT_MDPI))

        assertBeneathClicks(1)
    }

    /** The second tap of a burst, ignored. Returns where and when it lifted. */
    private fun removeThenSecondTapBeneath(): Pair<Offset, Long> {
        val (point, liftedAt) = removeThroughTheMenu()
        goBeneathAfter(liftedAt, UNDER_THE_LIMIT)
        val secondLiftedAt = pressBeneathAt(point)
        assertBeneathClicks(0)
        return point to secondLiftedAt
    }

    @Test
    fun aThirdPressUnderTheBurstLimitAfterTheSecondsLiftDoesNothing() {
        val (point, secondLiftedAt) = removeThenSecondTapBeneath()

        goToAfter(secondLiftedAt, UNDER_THE_BURST_LIMIT)
        pressBeneathAt(point)

        assertBeneathClicks(0)
    }

    @Test
    fun aThirdPressPastTheBurstLimitAfterTheSecondsLiftAnswers() {
        val (point, secondLiftedAt) = removeThenSecondTapBeneath()

        goToAfter(secondLiftedAt, PAST_THE_BURST_LIMIT)
        pressBeneathAt(point)

        assertBeneathClicks(1)
    }

    /** As when the archived tag leaves the list: the lift is in the rule before the item runs. */
    @Test
    fun aPressBeneathDoesNothingWhenTheItemTookTheMenuAway() {
        itemRemovesTheAnchor = true
        val (point, liftedAt) = removeThroughTheMenu()

        goBeneathAfter(liftedAt, UNDER_THE_LIMIT)
        pressBeneathAt(point)

        assertBeneathClicks(0)
    }

    @Test
    fun theItemAnswersItsFirstTapAtOnce() {
        showOverAnOlderDialog()
        openMenu()

        harness.tap(item())

        composeRule.runOnIdle { assertEquals(1, itemClicks) }
    }

    @Test
    fun aClickActionOnTheItemIsNoLift() {
        showOverAnOlderDialog()
        openMenuAndLetItSettle()
        val point = item().centreOnScreen()
        // Not performClick(), which is a touch.
        item().performSemanticsAction(SemanticsActions.OnClick)
        val clickedAt = composeRule.mainClock.currentTime
        composeRule.runOnIdle { assertEquals(1, itemClicks) }

        goBeneathAfter(clickedAt, UNDER_THE_LIMIT)
        pressBeneathAt(point)

        assertBeneathClicks(1)
    }

    /** The menu fades out after the item closed it, and a tap there reaches it again. */
    @Test
    fun aSecondTapOnTheClosingMenuRunsTheItemAgainAndNothingElse() {
        val (point, liftedAt) = removeThroughTheMenu()
        assertTrue("the menu is still fading out", isMenuUp())

        goToAfter(liftedAt, 1L)
        assertEquals("the same point", point, item().centreOnScreen())
        harness.tap(item())

        composeRule.runOnIdle { assertEquals(2, itemClicks) }
        assertMenuGone()
        assertBeneathClicks(0)
    }

    private fun menuTree() = composeRule.onNode(isPopup(), useUnmergedTree = true)
        .printToString(Int.MAX_VALUE)
        .replace(Regex("Node #\\d+"), "Node")

    @Test
    fun theMenusTreeAndBoundsMatchARawOne() {
        showOverAnOlderDialog(raw)
        openMenuAndLetItSettle()
        val rawTree = menuTree()
        harness.hide()
        composeRule.runOnUiThread { expanded = false }

        showOverAnOlderDialog()
        openMenuAndLetItSettle()

        assertEquals(rawTree, menuTree())
        assertTrue("the tree has the item", rawTree.contains(REMOVE))
    }
}
