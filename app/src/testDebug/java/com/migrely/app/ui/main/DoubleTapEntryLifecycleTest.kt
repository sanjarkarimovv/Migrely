package com.migrely.app.ui.main

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val FIRST = "first"
private const val NEXT = "next"
private const val NEXT_TOP_DP = 300
private const val NEXT_POINT_Y_DP = 340
private const val POINT_X_DP = 200
private const val FIRST_TICK = 3
private const val LAST_TICK = 24
private const val MOST_FRAMES = 60
private const val SETTLE_MILLIS = 600L

@Serializable
internal data object LifecycleFirstRoute

@Serializable
internal data object LifecycleNextRoute

/**
 * The mark is written on the entry's saved state, which throws once the entry is destroyed. The
 * entry can be destroyed and still composed: a pop that lands after the frame where the enter fade
 * finishes and before `NavHost`'s completion effect runs completes the entry at once, and it stays
 * composed until the next frame. A lift in that gap, as the first access to the handle, must not
 * crash. The frame moves with the fade, so every tick is tried, and the test fails if none hit it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w400dp-h1200dp")
class DoubleTapEntryLifecycleTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var navController: NavHostController
    private var popAtTick = -1
    private var popped = false

    @Before
    fun setUp() {
        composeRule.setContent {
            navController = rememberNavController()
            Box(Modifier.fillMaxSize(), propagateMinConstraints = true) {
                ScreenNavHost(navController, startDestination = LifecycleFirstRoute) {
                    screen<LifecycleFirstRoute>(navController) {
                        Box(Modifier.fillMaxSize()) { Control(FIRST, 0) }
                    }
                    screen<LifecycleNextRoute>(navController) {
                        if (popAtTick >= 0) Ticker(::popAt)
                        Box(Modifier.fillMaxSize()) { Control(NEXT, NEXT_TOP_DP) }
                    }
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.mainClock.autoAdvance = false
    }

    private fun popAt(tick: Int) {
        if (tick == popAtTick) {
            popAtTick = -1
            popped = true
            navController.popBackStack()
        }
    }

    @Test
    fun popBetweenTheFinishFrameAndTheCompletionEffect() {
        var liftsOnADestroyedEntry = 0
        for (tick in FIRST_TICK..LAST_TICK) {
            val entry = pushAndPopAt(tick)
            if (composeRule.onAllNodesWithTag(NEXT).fetchSemanticsNodes().isNotEmpty()) {
                press()
                if (entry.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                    liftsOnADestroyedEntry++
                }
                composeRule.onRoot().performTouchInput { up() }
                composeRule.waitForIdle()
            }
            composeRule.mainClock.advanceTimeBy(SETTLE_MILLIS)
            composeRule.waitForIdle()
            composeRule.onNodeWithTag(FIRST).assertExists()
        }

        assertTrue("no tick reached the gap, so this tests nothing", liftsOnADestroyedEntry > 0)
    }

    private fun pushAndPopAt(tick: Int): NavBackStackEntry {
        popped = false
        popAtTick = tick
        composeRule.runOnUiThread { navController.navigate(LifecycleNextRoute) }
        composeRule.waitForIdle()
        val entry = checkNotNull(navController.currentBackStackEntry)
        var frames = 0
        while (!popped && frames < MOST_FRAMES) {
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.waitForIdle()
            frames++
        }
        return entry
    }

    private fun press() {
        val point = with(composeRule.density) {
            Offset(POINT_X_DP.dp.toPx(), NEXT_POINT_Y_DP.dp.toPx())
        }
        composeRule.onRoot().performTouchInput { down(point) }
        composeRule.waitForIdle()
    }
}

/** Counts frames in composition, so a pop can land in a chosen frame's apply step. */
@Composable
private fun Ticker(onTick: (Int) -> Unit) {
    var tick by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { }
            tick++
        }
    }
    val current = tick
    SideEffect { onTick(current) }
}

@Composable
private fun Control(tag: String, topDp: Int) {
    Box(
        Modifier
            .offset(y = topDp.dp)
            .fillMaxWidth()
            .height(80.dp)
            .testTag(tag)
            .clickable { }
    )
}
