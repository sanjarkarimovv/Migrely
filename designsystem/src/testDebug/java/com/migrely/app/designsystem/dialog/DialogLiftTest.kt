package com.migrely.app.designsystem.dialog

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val OLDER = "older"
private const val UPPER = "upper"
private const val PAST_THE_WAIT = 800L
private const val IN_THE_WAIT = 300L
private const val UNDER_THE_LIMIT = 100L
private const val PAST_THE_LIMIT = 600L
private const val FRAMES = 5
private const val NEW = "new"
private const val DRAG_STEPS = 3
private const val DRAG_STEP_PX = -60f
private const val OLDER_PAGE_HEIGHT_DP = 3_000
private const val SECOND_TAP_EARLY = 150L
private const val SECOND_TAP_LATE = 350L
private const val JUST_AFTER_B_S_WAIT = DIALOG_WAIT_MILLIS + 10L
private const val THIRD_TAP_AFTER_THE_SECONDS_LIFT = 300L
private const val PAST_THE_SECONDS_LIFT = 520L
private const val BEYOND_DP_AT_MDPI = 350f
private const val XXHDPI = 3f
private const val INSIDE_THE_SLOP_DP = 90f
private const val OUTSIDE_THE_SLOP_DP = 110f

/**
 * A dialog over a dialog, in one test's windows: a lift on the upper one counts for the older one,
 * at screen positions read from the nodes (BRD D161). Robolectric's windows share an origin, so
 * that the two windows' origins agree is proved on a device, by the androidTest in `app`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w400dp-h800dp")
class DialogLiftTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val harness = DialogWaitHarness(composeRule)
    private var olderClicks by mutableIntStateOf(0)
    private var upperClicks by mutableIntStateOf(0)
    private var upperOpen by mutableStateOf(true)
    private var upperCloses = false
    private var opensNew = false
    private var newOpen by mutableStateOf(false)
    private var newClicks by mutableIntStateOf(0)

    @Before
    fun setUp() = harness.setUp()

    private fun showBoth(older: @Composable () -> Unit = { ClickableOlder() }) = harness.show {
        MigrelyDialog(onDismissRequest = {}, fullScreen = true) {
            Surface { older() }
        }
        if (upperOpen) {
            MigrelyAlertDialog(
                onDismissRequest = {},
                confirmButton = {
                    TextButton(
                        onClick = {
                            upperClicks++
                            if (upperCloses) upperOpen = false
                            if (opensNew) newOpen = true
                        },
                        modifier = Modifier.testTag(UPPER)
                    ) {
                        Text("Do")
                    }
                }
            )
        }
        if (newOpen) {
            MigrelyAlertDialog(
                onDismissRequest = {},
                confirmButton = {
                    TextButton(
                        onClick = { newClicks++ },
                        modifier = Modifier.testTag(NEW)
                    ) {
                        Text("Do")
                    }
                }
            )
        }
    }

    @Composable
    private fun ClickableOlder() {
        Box(
            Modifier
                .fillMaxSize()
                .testTag(OLDER)
                .clickable { olderClicks++ }
        )
    }

    @Composable
    private fun ScrollingOlder(scroll: ScrollState) {
        Column(
            Modifier
                .fillMaxSize()
                .testTag(OLDER)
                .verticalScroll(scroll)
        ) {
            Spacer(Modifier.fillMaxWidth().height(OLDER_PAGE_HEIGHT_DP.dp))
        }
    }

    private fun upper() = harness.tagged(UPPER)

    private fun older() = harness.tagged(OLDER)

    private fun SemanticsNodeInteraction.centreOnScreen(): Offset {
        val node = fetchSemanticsNode()
        return node.positionOnScreen + Offset(node.size.width / 2f, node.size.height / 2f)
    }

    /** A press and lift on the older dialog at this point of the screen. */
    private fun pressOlderAt(onScreen: Offset) {
        val inside = onScreen - older().fetchSemanticsNode().positionOnScreen
        older().performTouchInput {
            down(inside)
            up()
        }
        composeRule.waitForIdle()
    }

    private fun after(millis: Long) {
        composeRule.mainClock.advanceTimeBy(millis, ignoreFrameDuration = true)
        composeRule.waitForIdle()
    }

    private fun frames() = repeat(FRAMES) {
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()
    }

    private fun assertOlderClicks(expected: Int) = composeRule.runOnIdle {
        assertEquals(expected, olderClicks)
    }

    private fun tapUpperPastTheWait(older: @Composable () -> Unit = { ClickableOlder() }): Offset {
        showBoth(older)
        harness.goTo(PAST_THE_WAIT)
        val point = upper().centreOnScreen()
        harness.tap(upper())
        composeRule.runOnIdle { assertEquals(1, upperClicks) }
        return point
    }

    @Test
    fun aPressOnTheOlderDialogUnderTheLimitDoesNothingAndAfterItAnswers() {
        val point = tapUpperPastTheWait()

        after(UNDER_THE_LIMIT)
        pressOlderAt(point)
        assertOlderClicks(0)

        after(PAST_THE_LIMIT)
        pressOlderAt(point)
        assertOlderClicks(1)
    }

    @Test
    fun aPressBeyondTheDistanceAnswers() {
        val point = tapUpperPastTheWait()

        after(UNDER_THE_LIMIT)
        pressOlderAt(point + Offset(0f, BEYOND_DP_AT_MDPI))

        assertOlderClicks(1)
    }

    @Test
    fun aLiftOnCancelCounts() {
        upperCloses = true
        val point = tapUpperPastTheWait()
        frames()
        upper().assertDoesNotExist()

        pressOlderAt(point)

        assertOlderClicks(0)
    }

    @Test
    fun aPressTheWaitIgnoredDoesNotCount() {
        upperOpen = false
        showBoth()
        harness.goTo(PAST_THE_WAIT)
        composeRule.runOnUiThread { upperOpen = true }
        frames()
        val point = upper().centreOnScreen()
        after(IN_THE_WAIT)
        harness.tap(upper())
        composeRule.runOnIdle { assertEquals(0, upperClicks) }

        composeRule.runOnUiThread { upperOpen = false }
        frames()
        pressOlderAt(point)

        assertOlderClicks(1)
    }

    private fun dragOlderAt(onScreen: Offset) {
        val inside = onScreen - older().fetchSemanticsNode().positionOnScreen
        older().performTouchInput {
            down(inside)
            repeat(DRAG_STEPS) { moveBy(Offset(0f, DRAG_STEP_PX)) }
            up()
        }
        composeRule.waitForIdle()
    }

    @Test
    fun aDragOnTheOlderDialogAfterALiftDoesNotScrollAndTheNextDoes() {
        val scroll = ScrollState(0)
        val point = tapUpperPastTheWait { ScrollingOlder(scroll) }

        after(UNDER_THE_LIMIT)
        dragOlderAt(point)
        assertEquals("the drag after the lift", 0, composeRule.runOnIdle { scroll.value })

        after(PAST_THE_LIMIT)
        dragOlderAt(point)
        assertTrue("the next drag", composeRule.runOnIdle { scroll.value } > 0)
    }

    private fun tapNew() = harness.tap(harness.tagged(NEW))

    private fun assertNewClicks(expected: Int) = composeRule.runOnIdle {
        assertEquals(expected, newClicks)
    }

    /** A's button closes A and opens B under the finger, as Next does for the time step. */
    private fun tapAThatOpensB() {
        upperCloses = true
        opensNew = true
        tapUpperPastTheWait()
        frames()
        harness.tagged(NEW).assertExists()
    }

    /** Goes to [secondAt] after B appeared, taps B in its wait, then to [thirdAt], and taps B. */
    private fun doubleTapOnAThenTapB(secondAt: Long, thirdAt: Long) {
        tapAThatOpensB()
        harness.goTo(secondAt)
        tapNew()
        assertNewClicks(0)
        harness.goTo(thirdAt)
        tapNew()
    }

    @Test
    fun afterADoubleTapOnABJustAfterItsWaitAnswers() {
        doubleTapOnAThenTapB(secondAt = SECOND_TAP_EARLY, thirdAt = JUST_AFTER_B_S_WAIT)

        assertNewClicks(1)
    }

    /** The second tap late in B's wait, so that this press is under the burst limit of its lift. */
    @Test
    fun afterADoubleTapOnALateInBsWaitBJustAfterItsWaitAnswers() {
        doubleTapOnAThenTapB(secondAt = SECOND_TAP_LATE, thirdAt = JUST_AFTER_B_S_WAIT)

        assertNewClicks(1)
    }

    @Test
    fun afterADoubleTapOnABThreeHundredAndThirtyAfterItsWaitAnswers() {
        doubleTapOnAThenTapB(secondAt = SECOND_TAP_EARLY, thirdAt = DIALOG_WAIT_MILLIS + 330L)

        assertNewClicks(1)
    }

    @Test
    fun afterADoubleTapOnABAfterAPauseAnswers() {
        doubleTapOnAThenTapB(secondAt = SECOND_TAP_EARLY, thirdAt = DIALOG_WAIT_MILLIS + 600L)

        assertNewClicks(1)
    }

    @Test
    fun afterASingleTapOnABJustAfterItsWaitAnswers() {
        tapAThatOpensB()
        harness.goTo(JUST_AFTER_B_S_WAIT)

        tapNew()

        assertNewClicks(1)
    }

    /**
     * Why the node still tells the rule of a press it ignored twice over. A stays up under B, and a
     * second tap reaches A before B's window is up, so that lift comes after B appeared. A third
     * tap on B is then in B's wait and also within that lift. Its lift moves the lift, so a press
     * beneath past the limit after the second's lift and under the burst's after the third's is
     * held off.
     */
    @Test
    fun aPressIgnoredByTheWaitAndTheRuleMovesTheLift() {
        opensNew = true
        tapUpperPastTheWait()
        frames()
        val point = upper().centreOnScreen()
        harness.tap(upper())
        val secondLiftAt = composeRule.mainClock.currentTime
        composeRule.runOnIdle { assertEquals("A answers its own second tap", 2, upperClicks) }

        after(THIRD_TAP_AFTER_THE_SECONDS_LIFT)
        tapNew()
        assertNewClicks(0)
        composeRule.runOnUiThread {
            newOpen = false
            upperOpen = false
        }
        frames()
        after(secondLiftAt + PAST_THE_SECONDS_LIFT - composeRule.mainClock.currentTime)
        pressOlderAt(point)

        assertOlderClicks(0)
    }

    @Test
    fun twoTapsInsideOneDialogBothCount() {
        tapUpperPastTheWait()

        after(UNDER_THE_LIMIT)
        harness.tap(upper())

        composeRule.runOnIdle { assertEquals(2, upperClicks) }
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-xxhdpi")
    fun atXxhdpiNinetyDpFromTheLiftIsIgnored() {
        val point = tapUpperPastTheWait()

        after(UNDER_THE_LIMIT)
        pressOlderAt(point + Offset(0f, INSIDE_THE_SLOP_DP * XXHDPI))

        assertOlderClicks(0)
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-xxhdpi")
    fun atXxhdpiAHundredAndTenDpFromTheLiftAnswers() {
        val point = tapUpperPastTheWait()

        after(UNDER_THE_LIMIT)
        pressOlderAt(point + Offset(0f, OUTSIDE_THE_SLOP_DP * XXHDPI))

        assertOlderClicks(1)
    }
}
