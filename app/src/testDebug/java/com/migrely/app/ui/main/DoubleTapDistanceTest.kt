package com.migrely.app.ui.main

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val OPEN = "open"
private const val AT_90_DP = "at-90-dp"
private const val AT_110_DP = "at-110-dp"
private const val OPEN_HEIGHT_DP = 80
private const val TARGET_HEIGHT_DP = 16
private const val GAP_MILLIS = 100L

@Serializable
internal data object DistanceFirstRoute

@Serializable
internal data object DistanceNextRoute

/** The platform's distance is in pixels and so are the touches: at three times density it is 100 dp. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w400dp-h1200dp-xxhdpi")
class DoubleTapDistanceTest {
    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var navController: NavHostController
    private val near = mutableIntStateOf(0)
    private val far = mutableIntStateOf(0)

    @Before
    fun setUp() {
        composeRule.setContent {
            navController = rememberNavController()
            ScreenNavHost(navController, startDestination = DistanceFirstRoute) {
                screen<DistanceFirstRoute>(navController) {
                    Box(Modifier.fillMaxSize()) {
                        Target(OPEN, 0, OPEN_HEIGHT_DP) {
                            navController.navigate(DistanceNextRoute)
                        }
                    }
                }
                screen<DistanceNextRoute>(navController) {
                    // The first tap is at 40 dp; each target's centre is 90 or 110 dp below it.
                    Box(Modifier.fillMaxSize()) {
                        Target(AT_90_DP, 122, TARGET_HEIGHT_DP) { near.intValue++ }
                        Target(AT_110_DP, 142, TARGET_HEIGHT_DP) { far.intValue++ }
                    }
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.mainClock.autoAdvance = false
    }

    private fun tapThen(target: String) {
        composeRule.onNodeWithTag(OPEN).performTouchInput {
            down(center)
            up()
        }
        composeRule.waitForIdle()
        composeRule.mainClock.advanceTimeBy(GAP_MILLIS, ignoreFrameDuration = true)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(target).performTouchInput {
            down(center)
            up()
        }
        composeRule.waitForIdle()
    }

    @Test
    fun aSecondTapAt90DpIsIgnored() {
        tapThen(AT_90_DP)

        composeRule.runOnIdle { assertEquals(0, near.intValue) }
    }

    @Test
    fun aSecondTapAt110DpAnswers() {
        tapThen(AT_110_DP)

        composeRule.runOnIdle { assertEquals(1, far.intValue) }
    }
}

@Composable
private fun Target(tag: String, topDp: Int, heightDp: Int, onClick: () -> Unit) {
    Box(
        Modifier
            .offset(y = topDp.dp)
            .fillMaxWidth()
            .height(heightDp.dp)
            .testTag(tag)
            .clickable(onClick = onClick)
    )
}
