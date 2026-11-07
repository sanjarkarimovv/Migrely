package com.migrely.app.ui.main

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToDownIgnoreConsumed
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

private const val OPEN = "open"
private const val UNDER = "under"
private const val BACK = "back"
private const val BELOW_BACK = "below-back"
private const val BACK_TOP_DP = 200
private const val UNDER_THE_TIMEOUT_MILLIS = 299L
private const val AFTER_THE_TIMEOUT_MILLIS = 500L
private const val SHORT_GAP_MILLIS = 100L

@Serializable
internal data object RestoreFirstRoute

@Serializable
internal data object RestoreNextRoute

/**
 * A real activity is destroyed with no retained state, as a kill does, and a new one is made from
 * what the old one saved. So nothing lives across it but the saved state: `remember` is gone, and so
 * are the view models, and with them any handle not restored from the bundle. It says nothing about
 * the size of a real saved-state bundle or about Android's own process death on a device.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w400dp-h1200dp")
class DoubleTapRestorationTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private lateinit var navController: NavHostController
    private var underClicks = 0
    private var belowBackClicks = 0
    private var lastLift = 0L
    private var lastPress = 0L

    private fun show(saved: Bundle?): ActivityController<ComponentActivity> {
        val controller = Robolectric.buildActivity(ComponentActivity::class.java)
        controller.create(saved).start().resume().visible()
        setContentOn(controller)
        composeRule.waitForIdle()
        return controller
    }

    private fun setContentOn(controller: ActivityController<ComponentActivity>) {
        controller.get().setContent {
            navController = rememberNavController()
            Box(Modifier.fillMaxSize().pointerInput(Unit) { readTouchTimes() }) {
                ScreenNavHost(navController, startDestination = RestoreFirstRoute) {
                    screen<RestoreFirstRoute>(navController) {
                        Box(Modifier.fillMaxSize()) {
                            Control(OPEN, 0) { navController.navigate(RestoreNextRoute) }
                            Control(BELOW_BACK, BACK_TOP_DP) { belowBackClicks++ }
                        }
                    }
                    screen<RestoreNextRoute>(navController) {
                        Box(Modifier.fillMaxSize()) {
                            Control(UNDER, 0) { underClicks++ }
                            Control(BACK, BACK_TOP_DP) { navController.popBackStack() }
                        }
                    }
                }
            }
        }
    }

    private suspend fun PointerInputScope.readTouchTimes() = awaitPointerEventScope {
        while (true) {
            awaitPointerEvent(PointerEventPass.Initial).changes.forEach {
                if (it.changedToDownIgnoreConsumed()) lastPress = it.uptimeMillis
                if (it.changedToUpIgnoreConsumed()) lastLift = it.uptimeMillis
            }
        }
    }

    private fun stopTheClock() {
        composeRule.mainClock.autoAdvance = false
    }

    private fun advance(millis: Long) {
        composeRule.mainClock.advanceTimeBy(millis, ignoreFrameDuration = true)
        composeRule.waitForIdle()
    }

    /** The press after this tap came this long after the lift before it, by the events' time. */
    private fun assertPressGap(lift: Long, gap: Long) = assertEquals(gap, lastPress - lift)

    private fun recreate(
        old: ActivityController<ComponentActivity>
    ): ActivityController<ComponentActivity> {
        val saved = Bundle()
        old.saveInstanceState(saved)
        // Not a configuration change: the view models go, as they do when the process dies.
        old.pause().stop().destroy()
        return show(saved)
    }

    private fun tap(tag: String) {
        composeRule.onNodeWithTag(tag).performTouchInput {
            down(center)
            up()
        }
        composeRule.waitForIdle()
    }

    private fun entryIds() = composeRule.runOnIdle {
        navController.previousBackStackEntry?.id to navController.currentBackStackEntry?.id
    }

    @Test
    fun theEntryIdsSurviveARestore() {
        val first = show(null)
        tap(OPEN)
        composeRule.mainClock.advanceTimeBy(AFTER_THE_TIMEOUT_MILLIS)
        val before = entryIds()
        assertNotEquals(null, before.first)

        recreate(first)

        assertEquals(before, entryIds())
    }

    @Test
    fun aReturnToATouchedScreenAfterARestoreAnswersUnderTheTimeout() {
        val first = show(null)
        tap(OPEN)
        composeRule.mainClock.advanceTimeBy(AFTER_THE_TIMEOUT_MILLIS)
        recreate(first)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(BACK).assertExists()
        stopTheClock()

        tap(BACK)
        val lift = lastLift
        advance(UNDER_THE_TIMEOUT_MILLIS)
        tap(BELOW_BACK)

        assertPressGap(lift, UNDER_THE_TIMEOUT_MILLIS)
        assertEquals(1, composeRule.runOnIdle { belowBackClicks })
    }

    /** A recreation with the view models kept, as a rotation does. */
    @Test
    fun aReturnToATouchedScreenAfterARotationAnswersUnderTheTimeout() {
        val controller = show(null)
        tap(OPEN)
        composeRule.mainClock.advanceTimeBy(AFTER_THE_TIMEOUT_MILLIS)
        val before = entryIds()

        controller.recreate()
        setContentOn(controller)
        composeRule.waitForIdle()
        assertEquals(before, entryIds())
        stopTheClock()

        tap(BACK)
        val lift = lastLift
        advance(UNDER_THE_TIMEOUT_MILLIS)
        tap(BELOW_BACK)

        assertPressGap(lift, UNDER_THE_TIMEOUT_MILLIS)
        assertEquals(1, composeRule.runOnIdle { belowBackClicks })
    }

    @Test
    fun aTapRightAfterARestoreAnswersBecauseTheLastLiftIsNotKept() {
        val first = show(null)
        stopTheClock()
        tap(OPEN)
        val lift = lastLift

        recreate(first)
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.waitForIdle()
        tap(UNDER)

        assertTrue("under the timeout", lastPress - lift < UNDER_THE_TIMEOUT_MILLIS)
        assertEquals(1, composeRule.runOnIdle { underClicks })
    }

    @Test
    fun aNewEntryOfTheSameDestinationAfterAPopHasNoMark() {
        show(null)
        tap(OPEN)
        composeRule.mainClock.advanceTimeBy(AFTER_THE_TIMEOUT_MILLIS)
        tap(UNDER)
        assertEquals(1, composeRule.runOnIdle { underClicks })
        tap(BACK)
        composeRule.mainClock.advanceTimeBy(AFTER_THE_TIMEOUT_MILLIS)
        composeRule.waitForIdle()
        stopTheClock()

        tap(OPEN)
        val lift = lastLift
        advance(SHORT_GAP_MILLIS)
        tap(UNDER)

        assertPressGap(lift, SHORT_GAP_MILLIS)
        assertEquals(1, composeRule.runOnIdle { underClicks })
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
