package com.migrely.app.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertPositionInRootIsEqualTo
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.DpRect
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

private const val OPEN = "open"
private const val REMOVE = "remove"
private const val ONLY_ON_LIST = "only-on-list"
private const val SAVE = "save"
private const val ON_BOTH = "on-both"
private const val SMALL = "small"
private const val NO_SIZE = "no-size"
private const val FADE_STARTED_MILLIS = 100L
private const val LATE_IN_THE_FADE_MILLIS = 600L
private const val AFTER_THE_FADE_MILLIS = 1_000L

@Serializable
private data object ListRoute

@Serializable
private data object EditorRoute

@Serializable
private data object PlainRoute

@Serializable
private data object WrappedRoute

/**
 * A real `NavHost` with its default 700 ms fade and a stopped clock. The list has a row that opens
 * the editor and a control that writes (Remove in My medications, Delete on details). Issue #142:
 * touches on a screen that is leaving must do nothing; the screen fading in must keep working.
 */
class LeavingScreenInputTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var navController: NavHostController
    private val clicks = mapOf(
        REMOVE to mutableIntStateOf(0),
        ONLY_ON_LIST to mutableIntStateOf(0),
        SAVE to mutableIntStateOf(0),
        ON_BOTH to mutableIntStateOf(0)
    )

    @Before
    fun setUp() {
        composeRule.setContent {
            navController = rememberNavController()
            // As the app's Surface hands the nav host its size.
            Box(Modifier.fillMaxSize(), propagateMinConstraints = true) {
                Host()
            }
        }
        composeRule.waitForIdle()
        composeRule.mainClock.autoAdvance = false
    }

    @Composable
    private fun Host() {
        NavHost(navController, startDestination = ListRoute) {
            screen<ListRoute>(navController) {
                Box(Modifier.fillMaxSize()) {
                    Control(OPEN, 0) { navController.navigate(EditorRoute) }
                    Control(REMOVE, 100) { count(REMOVE) }
                    Control(ONLY_ON_LIST, 300) { count(ONLY_ON_LIST) }
                }
            }
            screen<EditorRoute>(navController) {
                Box(Modifier.fillMaxSize()) {
                    Control(ON_BOTH, 300) { count(ON_BOTH) }
                    Control(SAVE, 500) { count(SAVE) }
                    Box(Modifier.size(20.dp).testTag(SMALL))
                }
            }
            // The same screen with and without the wrapper, to compare what the host gives it.
            composable<PlainRoute> { Box(Modifier.testTag(NO_SIZE)) }
            screen<WrappedRoute>(navController) { Box(Modifier.testTag(NO_SIZE)) }
        }
    }

    private fun count(tag: String) {
        clicks.getValue(tag).intValue++
    }

    @Test
    fun aClickOnTheCurrentScreenCounts() {
        tap(REMOVE)

        assertClicks(REMOVE, 1)
    }

    @Test
    fun aFingerRestingOnAControlDoesNotClickItWhenLiftedEarlyInTheFade() {
        restOn(REMOVE)
        tap(OPEN)
        advance(FADE_STARTED_MILLIS)

        lift(REMOVE)

        assertClicks(REMOVE, 0)
    }

    @Test
    fun aFingerRestingOnAControlDoesNotClickItWhenLiftedLateInTheFade() {
        restOn(REMOVE)
        tap(OPEN)
        advance(LATE_IN_THE_FADE_MILLIS)
        composeRule.onNodeWithTag(REMOVE).assertExists()

        lift(REMOVE)

        assertClicks(REMOVE, 0)
    }

    @Test
    fun aNewTouchFallingThroughTheEnteringScreenDoesNotReachTheLeavingOne() {
        tap(OPEN)
        advance(FADE_STARTED_MILLIS)

        tap(REMOVE)

        assertClicks(REMOVE, 0)
    }

    @Test
    fun aTouchOnTheLeavingScreenDuringAPopDoesNothing() {
        openEditorAndSettle()
        composeRule.runOnUiThread { navController.popBackStack() }
        composeRule.waitForIdle()
        advance(FADE_STARTED_MILLIS)

        tap(SAVE)

        assertClicks(SAVE, 0)
    }

    @Test
    fun theEnteringScreenTakesATapDuringAPush() {
        tap(OPEN)
        advance(FADE_STARTED_MILLIS)

        tap(SAVE)

        assertClicks(SAVE, 1)
    }

    @Test
    fun theEnteringScreenTakesATapDuringAPopWhereTheLeavingOneHasNoControl() {
        openEditorAndSettle()
        composeRule.runOnUiThread { navController.popBackStack() }
        composeRule.waitForIdle()
        advance(FADE_STARTED_MILLIS)

        tap(REMOVE)

        assertClicks(REMOVE, 1)
    }

    @Test
    fun aRestingFingerClicksWhenNothingNavigated() {
        restOn(REMOVE)
        advance(LATE_IN_THE_FADE_MILLIS)

        lift(REMOVE)

        assertClicks(REMOVE, 1)
    }

    @Test
    fun theSameControlWorksAgainOnReturningToTheScreen() {
        openEditorAndSettle()
        composeRule.runOnUiThread { navController.popBackStack() }
        composeRule.waitForIdle()
        advance(AFTER_THE_FADE_MILLIS)

        tap(REMOVE)

        assertClicks(REMOVE, 1)
    }

    @Test
    fun aScreenStaysWhereItWasAndKeepsItsSize() {
        openEditorAndSettle()

        composeRule.onNodeWithTag(SMALL).assertPositionInRootIsEqualTo(0.dp, 0.dp)
        composeRule.onNodeWithTag(SMALL).assertIsDisplayed()
        val root = composeRule.onRoot().getUnclippedBoundsInRoot()
        val save = composeRule.onNodeWithTag(SAVE).getUnclippedBoundsInRoot()
        assertEquals(root.left, save.left)
        assertEquals(root.right, save.right)
        composeRule.onNodeWithTag(SAVE).assertTopPositionInRootIsEqualTo(500.dp)
    }

    @Test
    fun aScreenGetsWhatTheHostGivesAScreenWithoutTheWrapper() {
        val plain = settledBoundsOf(PlainRoute)
        val wrapped = settledBoundsOf(WrappedRoute)

        assertEquals(plain, wrapped)
    }

    private fun settledBoundsOf(route: Any): DpRect {
        composeRule.runOnUiThread { navController.navigate(route) }
        composeRule.waitForIdle()
        advance(AFTER_THE_FADE_MILLIS)
        return composeRule.onNodeWithTag(NO_SIZE).getUnclippedBoundsInRoot()
    }

    @Test
    fun theSemanticsOfALeavingScreenAreNotTouched() {
        tap(OPEN)
        advance(FADE_STARTED_MILLIS)

        composeRule.onNodeWithTag(ONLY_ON_LIST).performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitForIdle()

        assertClicks(ONLY_ON_LIST, 1)
    }

    private fun openEditorAndSettle() {
        tap(OPEN)
        advance(AFTER_THE_FADE_MILLIS)
    }

    private fun node(tag: String): SemanticsNodeInteraction = composeRule.onNodeWithTag(tag)

    private fun tap(tag: String) {
        node(tag).performTouchInput {
            down(center)
            up()
        }
        composeRule.waitForIdle()
    }

    private fun restOn(tag: String) {
        node(tag).performTouchInput { down(1, center) }
        composeRule.waitForIdle()
    }

    private fun lift(tag: String) {
        node(tag).performTouchInput { up(1) }
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

@Composable
private fun Control(tag: String, topDp: Int, onClick: () -> Unit) {
    Box(
        Modifier
            .offset(y = topDp.dp)
            .fillMaxWidth()
            .height(80.dp)
            .testTag(tag)
            .clickable(onClick = onClick)
    )
}
