package com.migrely.app.ui.main

import android.app.Application
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.printToString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val OPEN = "open"
private const val UNDER = "under"
private const val ELSEWHERE = "elsewhere"
private const val BACK = "back"
private const val BELOW_BACK = "below-back"
private const val PAGE = "page"
private const val OPEN_PAGE = "open-page"
private const val STAY = "stay"
private const val UNDER_STAY = "under-stay"
private const val REST = "rest"
private const val OPEN_FIELD = "open-field"
private const val FIELD = "field"
private const val FADE_OVER_MILLIS = 300L
private const val AFTER_THE_TIMEOUT_MILLIS = 500L
private const val FAR_DP = 400
private const val PAGE_POINT_X_DP = 200
private const val OPEN_PAGE_CENTRE_Y_DP = 440
private const val DRAG_PX = -300f
private const val HOLD_MILLIS = 2_000L
private const val STAY_TOP_DP = 600
private const val REST_TOP_DP = 800
private const val OPEN_FIELD_TOP_DP = 1000
private const val BURST_TAPS = 10

@Serializable
internal data object FirstScreenRoute

@Serializable
internal data object NextScreenRoute

@Serializable
internal data object PageRoute

@Serializable
internal data object FieldRoute

@Serializable
internal data object PlainTreeRoute

@Serializable
internal data object GuardedTreeRoute

/**
 * The app's `ScreenNavHost` and `screen()`, a stopped clock, and one finger on a control that sits
 * under the first one on the next screen. Each test reads the gap back from the touch events, so
 * its name is what it did (issue #156, BRD D159).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w400dp-h1200dp")
class DoubleTapGuardTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var navController: NavHostController
    private lateinit var scroll: ScrollState
    private val clicks = mapOf(
        UNDER to mutableIntStateOf(0),
        ELSEWHERE to mutableIntStateOf(0),
        BELOW_BACK to mutableIntStateOf(0),
        STAY to mutableIntStateOf(0),
        UNDER_STAY to mutableIntStateOf(0),
        REST to mutableIntStateOf(0)
    )
    private var haptics = 0
    private val fakeHaptics = object : HapticFeedback {
        override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) {
            haptics++
        }
    }
    private var lastLift = 0L
    private var lastPress = 0L

    @Before
    fun setUp() {
        composeRule.setContent {
            navController = rememberNavController()
            Box(
                Modifier.fillMaxSize().pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent(PointerEventPass.Initial).changes.forEach {
                                if (it.changedToDownIgnoreConsumed()) lastPress = it.uptimeMillis
                                if (it.changedToUpIgnoreConsumed()) lastLift = it.uptimeMillis
                            }
                        }
                    }
                },
                propagateMinConstraints = true
            ) {
                CompositionLocalProvider(LocalHapticFeedback provides fakeHaptics) { Host() }
            }
        }
        composeRule.waitForIdle()
        composeRule.mainClock.autoAdvance = false
    }

    @Composable
    private fun Host() {
        ScreenNavHost(navController, startDestination = FirstScreenRoute) {
            screen<FirstScreenRoute>(navController) {
                Box(Modifier.fillMaxSize()) {
                    Control(OPEN, 0) { navController.navigate(NextScreenRoute) }
                    Control(BELOW_BACK, 200) { count(BELOW_BACK) }
                    Control(OPEN_PAGE, 400) { navController.navigate(PageRoute) }
                    Control(STAY, STAY_TOP_DP) { count(STAY) }
                    Control(REST, REST_TOP_DP) { count(REST) }
                    Control(OPEN_FIELD, OPEN_FIELD_TOP_DP) { navController.navigate(FieldRoute) }
                }
            }
            screen<NextScreenRoute>(navController) {
                Box(Modifier.fillMaxSize()) {
                    Control(UNDER, 0) { count(UNDER) }
                    Control(BACK, 200) { navController.popBackStack() }
                    Control(ELSEWHERE, FAR_DP) { count(ELSEWHERE) }
                    Control(UNDER_STAY, STAY_TOP_DP) { count(UNDER_STAY) }
                }
            }
            screen<PageRoute>(navController) {
                scroll = rememberScrollState()
                Column(Modifier.fillMaxSize().verticalScroll(scroll).testTag(PAGE)) {
                    Box(Modifier.fillMaxWidth().height(3_000.dp))
                }
            }
            screen<FieldRoute>(navController) {
                var text by remember { mutableStateOf("text") }
                Box(Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier
                            .offset(y = OPEN_FIELD_TOP_DP.dp)
                            .fillMaxWidth()
                            .testTag(FIELD)
                    )
                }
            }
            composable<PlainTreeRoute> { TreeContent() }
            screen<GuardedTreeRoute>(navController) { TreeContent() }
        }
    }

    private fun count(tag: String) {
        clicks.getValue(tag).intValue++
    }

    @Test
    fun aSecondTapAt100MillisecondsDoesNothing() = assertSecondTapIgnoredAt(100)

    @Test
    fun aSecondTapAt150MillisecondsDoesNothing() = assertSecondTapIgnoredAt(150)

    @Test
    fun aSecondTapAt299MillisecondsDoesNothing() = assertSecondTapIgnoredAt(299)

    @Test
    fun aSecondTapAt300MillisecondsCounts() = assertSecondTapCountsAt(300)

    @Test
    fun aSecondTapAt500MillisecondsCounts() = assertSecondTapCountsAt(500)

    private fun assertSecondTapIgnoredAt(gap: Long) {
        tap(OPEN)
        tapAfter(UNDER, gap)

        assertClicks(UNDER, 0)
    }

    private fun assertSecondTapCountsAt(gap: Long) {
        tap(OPEN)
        tapAfter(UNDER, gap)

        assertClicks(UNDER, 1)
    }

    @Test
    fun aTapBeyondTheDistanceUnderTheTimeoutCounts() {
        tap(OPEN)
        tapAfter(ELSEWHERE, 100)

        assertClicks(ELSEWHERE, 1)
    }

    @Test
    fun aThirdQuickTapDoesNothingAndAPauseThenAnswers() {
        tap(OPEN)
        tapAfter(UNDER, 100)
        tapAfter(UNDER, 100)
        assertClicks(UNDER, 0)

        tapAfter(UNDER, 300)

        assertClicks(UNDER, 1)
    }

    @Test
    fun aTapAfterBackLandsOnTheScreenBelowAsBefore() {
        tap(OPEN)
        advance(AFTER_THE_TIMEOUT_MILLIS)
        tap(BACK)
        tapAfter(BELOW_BACK, FADE_OVER_MILLIS - 1)

        assertClicks(BELOW_BACK, 1)
    }

    @Test
    fun aSecondReturnToAScreenRecomposedInBetweenStillAnswers() {
        tap(OPEN)
        advance(AFTER_THE_TIMEOUT_MILLIS)
        composeRule.onNodeWithTag(OPEN).assertDoesNotExist()
        tap(BACK)
        advance(AFTER_THE_TIMEOUT_MILLIS)
        composeRule.onNodeWithTag(OPEN).assertExists()
        tap(OPEN)
        advance(AFTER_THE_TIMEOUT_MILLIS)
        tap(BACK)
        tapAfter(BELOW_BACK, FADE_OVER_MILLIS - 1)

        assertClicks(BELOW_BACK, 1)
    }

    @Test
    fun aScreenOpenedWithNoTapTakesAPressAtOnce() {
        composeRule.runOnUiThread { navController.navigate(NextScreenRoute) }
        composeRule.waitForIdle()
        advance(50)

        tap(UNDER)

        assertClicks(UNDER, 1)
    }

    @Test
    fun aPressHeldPastTheTimeoutStaysIgnoredAndTheNextGestureAnswers() {
        tap(OPEN)
        advance(100)
        node(UNDER).performTouchInput { down(center) }
        composeRule.waitForIdle()
        advance(HOLD_MILLIS)
        node(UNDER).performTouchInput { up() }
        composeRule.waitForIdle()
        assertClicks(UNDER, 0)

        advance(AFTER_THE_TIMEOUT_MILLIS)
        tap(UNDER)

        assertClicks(UNDER, 1)
    }

    @Test
    fun aSemanticsClickAt50MillisecondsRuns() {
        tap(OPEN)
        advance(50)

        node(UNDER).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()

        assertClicks(UNDER, 1)
    }

    @Test
    fun aDragInsideTheLimitsIsIgnoredAndTheNextDragScrolls() {
        tap(OPEN_PAGE)
        advanceToGap(100)
        dragPage(from = pageNearTheTap())
        assertEquals(0, scrolled())

        advance(AFTER_THE_TIMEOUT_MILLIS)
        dragPage(from = pageNearTheTap())

        assertTrue("the next drag scrolls", scrolled() > 0)
    }

    @Test
    fun aDragFromTheTimeoutUpScrolls() {
        tap(OPEN_PAGE)
        advanceToGap(300)
        dragPage(from = pageNearTheTap())

        assertTrue("from the timeout the page takes its drag", scrolled() > 0)
    }

    @Test
    fun aDragBeyondTheDistanceUnderTheTimeoutScrolls() {
        tap(OPEN_PAGE)
        advanceToGap(100)
        dragPage(from = pageFarFromTheTap())

        assertTrue("away from the first tap the page takes its drag", scrolled() > 0)
    }

    @Test
    fun aDragThatStartsFarAndCrossesTheFirstTapKeepsScrolling() {
        tap(OPEN_PAGE)
        advanceToGap(100)
        dragPage(from = pageFarFromTheTap(), by = -with(composeRule.density) { FAR_DP.dp.toPx() })

        assertTrue("the rule judges once, at the press", scrolled() > 0)
    }

    @Test
    fun aSecondDragRightAfterTheFirstOnTheSameNewScreenScrolls() {
        tap(OPEN_PAGE)
        advanceToGap(300)
        dragPage(from = pageNearTheTap())
        val first = scrolled()

        advanceToGap(50)
        dragPage(from = pageNearTheTap())

        assertTrue("a drag lifted here makes the screen touched", scrolled() > first)
    }

    @Test
    fun aSecondTapOnTheSameScreenAfterAFirstThatAnsweredCounts() {
        tap(OPEN)
        advance(AFTER_THE_TIMEOUT_MILLIS)
        tap(UNDER)
        tapAfter(UNDER, 100)

        assertClicks(UNDER, 2)
    }

    /** Issue #156 review M3: one screen's cancel must not free a press ignored on another. */
    @Test
    fun aFingerRestingOnTheLeavingScreenDoesNotFreeAnIgnoredDrag() {
        node(REST).performTouchInput { down(1, center) }
        composeRule.waitForIdle()
        tap(OPEN_PAGE)
        advanceToGap(100)
        val press = pageNearTheTap()
        composeRule.onRoot().performTouchInput { down(2, press) }
        composeRule.waitForIdle()
        advance(AFTER_THE_TIMEOUT_MILLIS)
        node(REST).assertDoesNotExist()

        composeRule.onRoot().performTouchInput {
            moveBy(2, Offset(0f, DRAG_PX / 2))
            moveBy(2, Offset(0f, DRAG_PX / 2))
            up(2)
        }
        composeRule.waitForIdle()
        val scrolledWhileIgnored = scrolled()
        composeRule.onRoot().performTouchInput { up(1) }
        composeRule.waitForIdle()

        assertEquals(0, scrolledWhileIgnored)
    }

    /** The mark is written on the entry: its saved state must not be read after it is destroyed. */
    @Test
    fun aFingerLiftedOnAPoppedScreenDuringItsFadeDoesNotClickOrCrash() {
        tap(OPEN)
        advance(AFTER_THE_TIMEOUT_MILLIS)
        node(UNDER).performTouchInput { down(1, center) }
        composeRule.waitForIdle()
        tap(BACK)
        advance(100)

        composeRule.onRoot().performTouchInput { up(1) }
        composeRule.waitForIdle()
        advance(FADE_OVER_MILLIS)

        assertClicks(UNDER, 0)
    }

    @Test
    fun aScreenThatOpensItselfJustAfterATapIgnoresThePressAndTheNextAnswers() {
        tap(STAY)
        assertClicks(STAY, 1)
        composeRule.runOnUiThread { navController.navigate(NextScreenRoute) }
        composeRule.waitForIdle()
        tapAfter(UNDER_STAY, 100)
        assertClicks(UNDER_STAY, 0)

        tapAfter(UNDER_STAY, 300)

        assertClicks(UNDER_STAY, 1)
    }

    @Test
    fun aReturnToAScreenNeverTouchedIgnoresOnePressAndTheNextAnswers() {
        composeRule.runOnUiThread { navController.navigate(NextScreenRoute) }
        composeRule.waitForIdle()
        advance(FADE_OVER_MILLIS)
        tap(BACK)
        tapAfter(BELOW_BACK, FADE_OVER_MILLIS - 1)
        assertClicks(BELOW_BACK, 0)

        tapAfter(BELOW_BACK, 300)

        assertClicks(BELOW_BACK, 1)
    }

    @Test
    fun anotherFingerFarAwayAnswersWhileTheIgnoredPressIsDown() {
        tap(OPEN)
        advanceToGap(100)
        node(UNDER).performTouchInput { down(1, center) }
        composeRule.waitForIdle()

        tap(ELSEWHERE)
        node(UNDER).performTouchInput { up(1) }
        composeRule.waitForIdle()

        assertClicks(ELSEWHERE, 1)
        assertClicks(UNDER, 0)
    }

    @Test
    fun repeatedTappingAt100MillisecondsAnswersOnlyAfterAPause() = assertBurst(100)

    @Test
    fun repeatedTappingAt250MillisecondsAnswersOnlyAfterAPause() = assertBurst(250)

    private fun assertBurst(gap: Long) {
        tap(OPEN)
        repeat(BURST_TAPS) { tapAfter(UNDER, gap) }
        assertClicks(UNDER, 0)

        tapAfter(UNDER, 300)

        assertClicks(UNDER, 1)
    }

    @Test
    fun aShortIgnoredTapDoesNotFocusATextFieldAndTheNextDoes() {
        tap(OPEN_FIELD)
        tapAfter(FIELD, 100)
        node(FIELD).assertIsNotFocused()

        tapAfter(FIELD, 300)

        node(FIELD).assertIsFocused()
    }

    @Test
    fun anIgnoredPressShowsNothing() {
        tap(OPEN)
        advance(100)
        val before = tree()
        val bounds = node(UNDER).getUnclippedBoundsInRoot()

        node(UNDER).performTouchInput { down(center) }
        drawAFrame()
        pressMark(UNDER).assertDoesNotExist()
        assertEquals(before, tree())
        node(UNDER).performTouchInput { up() }
        drawAFrame()

        assertEquals(before, tree())
        assertEquals(bounds, node(UNDER).getUnclippedBoundsInRoot())
        assertEquals(0, haptics)
    }

    /** What the test above looks for does show on a press that answers. */
    @Test
    fun aPressThatAnswersShowsThePress() {
        tap(OPEN)
        advance(AFTER_THE_TIMEOUT_MILLIS)

        node(UNDER).performTouchInput { down(center) }
        drawAFrame()

        pressMark(UNDER).assertExists()
        node(UNDER).performTouchInput { up() }
    }

    /** The control merges its children's semantics, so the mark shows only in the unmerged tree. */
    private fun pressMark(tag: String) =
        composeRule.onNodeWithTag(pressedMark(tag), useUnmergedTree = true)

    private fun drawAFrame() {
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()
    }

    @Test
    fun theSemanticsTreeIsTheSameWithAndWithoutTheGuard() {
        val plain = treeOf(PlainTreeRoute)
        val guarded = treeOf(GuardedTreeRoute)

        assertEquals(plain, guarded)
        assertTrue("the tree has its controls", plain.contains("tree-a"))
    }

    private fun treeOf(route: Any): String {
        composeRule.runOnUiThread {
            navController.navigate(route) { popUpTo(FirstScreenRoute) }
        }
        composeRule.waitForIdle()
        advance(FADE_OVER_MILLIS)
        return tree()
    }

    private fun tree() = composeRule.onRoot(useUnmergedTree = true).printToString(Int.MAX_VALUE)
        .replace(Regex("Node #\\d+"), "Node")

    private fun pageNearTheTap() = with(composeRule.density) {
        Offset(PAGE_POINT_X_DP.dp.toPx(), OPEN_PAGE_CENTRE_Y_DP.dp.toPx())
    }

    private fun pageFarFromTheTap() = with(composeRule.density) {
        Offset(PAGE_POINT_X_DP.dp.toPx(), (OPEN_PAGE_CENTRE_Y_DP + FAR_DP).dp.toPx())
    }

    private fun scrolled() = composeRule.runOnIdle { scroll.value }

    private fun dragPage(from: Offset, by: Float = DRAG_PX) {
        composeRule.onNodeWithTag(PAGE).performTouchInput {
            down(from)
            moveBy(Offset(0f, by / 2))
            moveBy(Offset(0f, by / 2))
            up()
        }
        composeRule.waitForIdle()
    }

    private fun node(tag: String) = composeRule.onNodeWithTag(tag)

    private fun tap(tag: String) {
        node(tag).performTouchInput {
            down(center)
            up()
        }
        composeRule.waitForIdle()
    }

    private fun tapAfter(tag: String, gap: Long) {
        val liftBefore = lastLift
        advanceToGap(gap)
        tap(tag)
        assertEquals("the gap this test claims", gap, lastPress - liftBefore)
    }

    /** Moves the clock so that the next press comes [gap] after the last lift, by the events' time. */
    private fun advanceToGap(gap: Long) {
        val wanted = lastLift + gap - composeRule.mainClock.currentTime
        check(wanted >= 0) { "the clock is already past a gap of $gap" }
        composeRule.mainClock.advanceTimeBy(wanted, ignoreFrameDuration = true)
        composeRule.waitForIdle()
    }

    private fun advance(millis: Long) {
        composeRule.mainClock.advanceTimeBy(millis)
        composeRule.waitForIdle()
    }

    private fun assertClicks(tag: String, expected: Int) = composeRule.runOnIdle {
        assertEquals(expected, clicks.getValue(tag).intValue)
    }
}

private fun pressedMark(tag: String) = "$tag-pressed"

@Composable
private fun Control(tag: String, topDp: Int, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Box(
        Modifier
            .offset(y = topDp.dp)
            .fillMaxWidth()
            .height(80.dp)
            .testTag(tag)
            .clickable(interactionSource, indication = null, onClick = onClick)
    ) {
        if (pressed) Box(Modifier.testTag(pressedMark(tag)))
    }
}

@Composable
private fun TreeContent() {
    Column {
        Control("tree-a", 0) {}
        Control("tree-b", 0) {}
    }
}
