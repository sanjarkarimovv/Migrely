package com.migrely.app.attack.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpRect
import androidx.navigation.compose.rememberNavController
import com.migrely.app.attack.ui.model.AttackDetailsEventUiModel
import com.migrely.app.attack.ui.model.AttackMessageUiModel
import com.migrely.app.attack.ui.notification.AttackDetailsEventPresenter
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val STORAGE_FULL = "Couldn't save. Your phone's storage is full."

/**
 * A message ignores the keyboard (BRD D180 (4), EC-UX-41, D178 (6)): on Attack details at 200% font in a short landscape
 * window, where a message padded for a 250 dp keyboard would no longer fit below the top bar and would rise over Back,
 * the message's top and Back's bounds are the same with the keyboard open as without it, on Android 8 and 15.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MessageKeyboardTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val events = AttackDetailsEventPresenter()
    private val dependencies = attackDetailsTestDependencies(events)

    private fun showStorageFull() {
        rule.runOnUiThread { rule.activity.drawEdgeToEdge() }
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                MigrelyTheme {
                    dependencies.AttackDetails(
                        attackId = "a",
                        announceMerge = false,
                        navController = rememberNavController()
                    )
                }
            }
        }
        rule.waitForIdle()
        rule.runOnIdle {
            events.show(AttackDetailsEventUiModel.Message(AttackMessageUiModel.StorageFull))
        }
        rule.waitForIdle()
    }

    private fun message(): DpRect = rule.onNodeWithText(STORAGE_FULL).getUnclippedBoundsInRoot()

    private fun back(): DpRect =
        rule.onNodeWithContentDescription("Back").getUnclippedBoundsInRoot()

    private fun assertTheKeyboardMovesNothing() {
        showStorageFull()
        val messageBefore = message()
        val backBefore = back()

        rule.runOnUiThread { rule.activity.setKeyboard(KEYBOARD_DP) }
        rule.waitForIdle()

        // The keyboard reached the screen: its content now ends at the keyboard's top (D180 (2)).
        val view = rule.activity.window.decorView
        val keyboardTop = view.height / view.resources.displayMetrics.density - KEYBOARD_DP
        val content = rule.onNode(hasScrollAction()).getUnclippedBoundsInRoot()
        assertEquals("the content ignored the keyboard", keyboardTop, content.bottom.value, 0.5f)
        assertEquals("the message moved", messageBefore.top.value, message().top.value, 0.5f)
        assertEquals("Back moved", backBefore, back())
        assertTrue("the message ${message()} covers Back ${back()}", message().top >= back().bottom)
    }

    @Test
    @Config(sdk = [35], qualifiers = "w800dp-h360dp")
    fun `On Android 15 the keyboard moves neither the message nor Back`() =
        assertTheKeyboardMovesNothing()

    @Test
    @Config(sdk = [26], qualifiers = "w800dp-h360dp")
    fun `On Android 8 the keyboard moves neither the message nor Back`() =
        assertTheKeyboardMovesNothing()
}
