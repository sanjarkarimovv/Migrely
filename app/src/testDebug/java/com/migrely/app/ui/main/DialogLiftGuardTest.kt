package com.migrely.app.ui.main

import android.app.Application
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.migrely.app.designsystem.dialog.DIALOG_WAIT_MILLIS
import com.migrely.app.designsystem.dialog.DialogLiftRule
import com.migrely.app.designsystem.dialog.LocalDialogLiftRule
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
private const val NEXT_PAGE = "next-page"
private const val POINTER = 900L
private const val UNDER_THE_LIMIT = 100L
private const val JUST_UNDER_THE_LIMIT = 499L
private const val PAUSE = 600L
private const val FADE_OVER = 400L
private const val FAR_PX = 300f
private const val THIRD_PRESS_AFTER_THE_DIALOGS_LIFT = 560L
private const val DRAG_STEPS = 3
private const val DRAG_STEP_PX = -60f
private const val NEXT_PAGE_HEIGHT_DP = 3_000
private const val TOUCHED_KEY = "com.migrely.app.second_tap.touched"
private const val INSIDE_THE_SLOP_PX_AT_XXHDPI = 270f
private const val OUTSIDE_THE_SLOP_PX_AT_XXHDPI = 330f

@Serializable
internal data object DialogLiftFirstRoute

@Serializable
internal data object DialogLiftNextRoute

/**
 * The real `ScreenNavHost` and `screen()`, with a dialog's lift told to the provided instance
 * directly: a test here cannot touch a real dialog, because `LocalDialogClock` is internal to
 * `designsystem` (BRD D161).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w400dp-h800dp")
class DialogLiftGuardTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var navController: NavHostController
    private lateinit var lifts: DialogLiftRule
    private var pageClicks by mutableIntStateOf(0)
    private var nextClicks by mutableIntStateOf(0)
    private lateinit var nextScroll: ScrollState

    @Before
    fun setUp() {
        composeRule.setContent {
            navController = rememberNavController()
            ScreenNavHost(navController, startDestination = DialogLiftFirstRoute) {
                screen<DialogLiftFirstRoute>(navController) {
                    lifts = checkNotNull(LocalDialogLiftRule.current)
                    Box(
                        Modifier.fillMaxSize().testTag(PAGE).clickable { pageClicks++ }
                    )
                }
                screen<DialogLiftNextRoute>(navController) {
                    nextScroll = rememberScrollState()
                    Box(Modifier.fillMaxSize().testTag(NEXT_PAGE)) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(nextScroll)
                                .clickable { nextClicks++ }
                        ) {
                            Spacer(Modifier.fillMaxWidth().height(NEXT_PAGE_HEIGHT_DP.dp))
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.mainClock.autoAdvance = false
    }

    private fun page() = composeRule.onNodeWithTag(PAGE, useUnmergedTree = true)

    private fun SemanticsNodeInteraction.centreOnScreen(): Offset {
        val node = fetchSemanticsNode()
        return node.positionOnScreen + Offset(node.size.width / 2f, node.size.height / 2f)
    }

    private fun SemanticsNodeInteraction.tapAtCentre(offset: Offset = Offset.Zero) {
        performTouchInput {
            down(center + offset)
            up()
        }
        composeRule.waitForIdle()
    }

    /** A dialog's lift over the middle of the screen, this long before the next press. */
    private fun dialogLifted(before: Long, at: Offset = page().centreOnScreen()) {
        val now = composeRule.mainClock.currentTime
        composeRule.runOnUiThread { lifts.lift(Any(), POINTER, now, at) }
        composeRule.mainClock.advanceTimeBy(before, ignoreFrameDuration = true)
        composeRule.waitForIdle()
    }

    private fun touchTheScreenOnce() {
        page().tapAtCentre()
        composeRule.mainClock.advanceTimeBy(FADE_OVER, ignoreFrameDuration = true)
        composeRule.waitForIdle()
        assertEquals("the screen answers before any dialog's lift", 1, pageClicks)
        pageClicks = 0
    }

    private fun assertPageClicks(expected: Int) = composeRule.runOnIdle {
        assertEquals(expected, pageClicks)
    }

    @Test
    fun withNoLiftAPressAnswers() {
        page().tapAtCentre()

        assertPageClicks(1)
    }

    @Test
    fun aPressOnATouchedScreenUnderTheLimitAndWithinTheDistanceDoesNothing() {
        touchTheScreenOnce()

        dialogLifted(before = UNDER_THE_LIMIT)
        page().tapAtCentre()

        assertPageClicks(0)
    }

    @Test
    fun justUnderTheLimitItDoesNothing() {
        touchTheScreenOnce()

        dialogLifted(before = JUST_UNDER_THE_LIMIT)
        page().tapAtCentre()

        assertPageClicks(0)
    }

    @Test
    fun atTheLimitItCounts() {
        touchTheScreenOnce()

        dialogLifted(before = DIALOG_WAIT_MILLIS)
        page().tapAtCentre()

        assertPageClicks(1)
    }

    @Test
    fun beyondTheDistanceItCounts() {
        touchTheScreenOnce()

        dialogLifted(before = UNDER_THE_LIMIT)
        page().tapAtCentre(Offset(0f, FAR_PX))

        assertPageClicks(1)
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-xxhdpi")
    fun atXxhdpiNinetyDpFromTheLiftDoesNothing() {
        touchTheScreenOnce()

        dialogLifted(before = UNDER_THE_LIMIT)
        page().tapAtCentre(Offset(0f, INSIDE_THE_SLOP_PX_AT_XXHDPI))

        assertPageClicks(0)
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp-xxhdpi")
    fun atXxhdpiAHundredAndTenDpFromTheLiftCounts() {
        touchTheScreenOnce()

        dialogLifted(before = UNDER_THE_LIMIT)
        page().tapAtCentre(Offset(0f, OUTSIDE_THE_SLOP_PX_AT_XXHDPI))

        assertPageClicks(1)
    }

    /**
     * The third press comes past the limit after the dialog's lift and under it after the
     * second's, so it is ignored only if the second's lift moved the lift.
     */
    @Test
    fun aThirdPressInABurstIsMeasuredFromTheSecondsLift() {
        touchTheScreenOnce()
        val dialogLiftedAt = composeRule.mainClock.currentTime

        dialogLifted(before = UNDER_THE_LIMIT)
        page().tapAtCentre()
        val thirdAt = dialogLiftedAt + THIRD_PRESS_AFTER_THE_DIALOGS_LIFT
        composeRule.mainClock.advanceTimeBy(
            thirdAt - composeRule.mainClock.currentTime,
            ignoreFrameDuration = true
        )
        composeRule.waitForIdle()
        page().tapAtCentre()
        assertPageClicks(0)

        composeRule.mainClock.advanceTimeBy(PAUSE, ignoreFrameDuration = true)
        composeRule.waitForIdle()
        page().tapAtCentre()
        assertPageClicks(1)
    }

    private fun goToTheNextEntry() {
        composeRule.runOnUiThread { navController.navigate(DialogLiftNextRoute) }
        composeRule.mainClock.advanceTimeBy(FADE_OVER, ignoreFrameDuration = true)
        composeRule.waitForIdle()
    }

    private fun isMarked() = composeRule.runOnIdle {
        checkNotNull(navController.currentBackStackEntry).savedStateHandle.contains(TOUCHED_KEY)
    }

    private fun dragNext() {
        composeRule.onNodeWithTag(NEXT_PAGE, useUnmergedTree = true).performTouchInput {
            down(center)
            repeat(DRAG_STEPS) { moveBy(Offset(0f, DRAG_STEP_PX)) }
            up()
        }
        composeRule.waitForIdle()
    }

    @Test
    fun aPressOnANewEntryDoesNothingAndLeavesItUnmarked() {
        goToTheNextEntry()
        val next = composeRule.onNodeWithTag(NEXT_PAGE, useUnmergedTree = true)

        dialogLifted(before = UNDER_THE_LIMIT, at = next.centreOnScreen())
        next.tapAtCentre()

        composeRule.runOnIdle { assertEquals(0, nextClicks) }
        assertFalse("an ignored press does not mark the entry", isMarked())
    }

    /** The mark's key is read here as a literal, so the last two lines show it is the right one. */
    @Test
    fun aDragOnANewEntryAfterADialogsLiftDoesNothingAndLeavesItUnmarked() {
        goToTheNextEntry()
        val next = composeRule.onNodeWithTag(NEXT_PAGE, useUnmergedTree = true)

        dialogLifted(before = UNDER_THE_LIMIT, at = next.centreOnScreen())
        dragNext()

        assertFalse("an ignored drag does not mark the entry", isMarked())
        assertEquals("nor does it scroll", 0, composeRule.runOnIdle { nextScroll.value })

        composeRule.mainClock.advanceTimeBy(PAUSE, ignoreFrameDuration = true)
        composeRule.waitForIdle()
        dragNext()
        assertTrue("the next drag scrolls", composeRule.runOnIdle { nextScroll.value } > 0)
        assertTrue("and marks it", isMarked())
    }

    @Test
    fun aClickActionRuns() {
        touchTheScreenOnce()

        dialogLifted(before = UNDER_THE_LIMIT)
        page().performSemanticsAction(SemanticsActions.OnClick)

        assertPageClicks(1)
    }
}
