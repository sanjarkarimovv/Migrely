package com.migrely.app.architecture.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val ROOT = "root"
private const val SCREEN = "screen"
private const val NEXT = "next"
private const val PICKER = "picker"

/** A screen is told it left only when it stops being the top of the back stack (BRD D165 (5)). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ScreenLeaveObserverTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: NavHostController
    private var leaves = 0
    private var pickerOpen by mutableStateOf(false)

    /** Opens [SCREEN] over [ROOT], as Attack details opens over Home. */
    private fun show() {
        rule.setContent {
            navController = rememberNavController()
            NavHost(navController, startDestination = ROOT) {
                composable(ROOT) { Box(Modifier.testTag(ROOT)) }
                composable(SCREEN) {
                    ScreenLeaveObserver(navController) { leaves++ }
                    Box(Modifier.testTag(SCREEN))
                    if (pickerOpen) {
                        Dialog(onDismissRequest = { pickerOpen = false }) {
                            Box(Modifier.testTag(PICKER))
                        }
                    }
                }
                composable(NEXT) { Box(Modifier.testTag(NEXT)) }
            }
        }
        navigate { navController.navigate(SCREEN) }
        rule.onNodeWithTag(SCREEN).assertExists()
    }

    private fun navigate(action: () -> Unit) {
        rule.runOnUiThread(action)
        rule.waitForIdle()
    }

    @Test
    fun `Opening the screen is not leaving it`() {
        show()

        assertEquals(0, leaves)
    }

    @Test
    fun `Back leaves`() {
        show()

        navigate { navController.popBackStack() }

        rule.onNodeWithTag(ROOT).assertExists()
        assertEquals(1, leaves)
    }

    @Test
    fun `Going forward leaves`() {
        show()

        navigate { navController.navigate(NEXT) }

        rule.onNodeWithTag(NEXT).assertExists()
        assertEquals(1, leaves)
    }

    @Test
    fun `Coming back is not leaving again`() {
        show()
        navigate { navController.navigate(NEXT) }

        navigate { navController.popBackStack() }

        rule.onNodeWithTag(SCREEN).assertExists()
        assertEquals(1, leaves)
    }

    @Test
    fun `A dialog or picker on the screen is not leaving`() {
        show()

        pickerOpen = true
        rule.waitForIdle()
        rule.onNodeWithTag(PICKER).assertExists()
        pickerOpen = false
        rule.waitForIdle()

        assertEquals(0, leaves)
    }

    @Test
    fun `Home, Android Settings and back is not leaving`() {
        show()

        rule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        rule.waitForIdle()
        rule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        rule.waitForIdle()

        rule.onNodeWithTag(SCREEN).assertExists()
        assertEquals(0, leaves)
    }
}
