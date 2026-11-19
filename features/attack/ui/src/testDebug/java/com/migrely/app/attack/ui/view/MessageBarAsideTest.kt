package com.migrely.app.attack.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.DpRect
import androidx.navigation.compose.rememberNavController
import com.migrely.app.attack.ui.model.AttackDetailsEventUiModel
import com.migrely.app.attack.ui.model.AttackMessageUiModel
import com.migrely.app.attack.ui.notification.AttackDetailsEventPresenter
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val STORAGE_FULL = "Couldn't save. Your phone's storage is full."
private const val BACK = "Back"

/**
 * A message keeps its place while the top bar steps aside (BRD D180 (4), D192, EC-UX-76; ux R10; task 3.4): on Attack
 * details in QA's API 36 360 dp window at 200%, the message's bounds are the same before the keyboard, with the
 * keyboard up and the bar aside, and after the keyboard has gone and the bar is back.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], fontScale = 2f, qualifiers = WINDOW_360)
class MessageBarAsideTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val events = AttackDetailsEventPresenter()
    private val dependencies = attackDetailsTestDependencies(events)

    private fun showStorageFull() {
        rule.runOnUiThread { rule.activity.drawEdgeToEdge() }
        rule.setContent {
            MigrelyTheme {
                dependencies.AttackDetails(
                    attackId = "a",
                    announceMerge = false,
                    navController = rememberNavController()
                )
            }
        }
        rule.waitForIdle()
        setKeyboard(null)
        rule.runOnIdle {
            events.show(AttackDetailsEventUiModel.Message(AttackMessageUiModel.StorageFull))
        }
        rule.waitForIdle()
    }

    private fun setKeyboard(top: Int?) {
        rule.runOnUiThread { rule.activity.setShortWindowInsets(top) }
        rule.waitForIdle()
    }

    private fun message(): DpRect = rule.onNodeWithText(STORAGE_FULL).getUnclippedBoundsInRoot()

    private fun backInTree(): Boolean =
        rule.onAllNodes(hasContentDescription(BACK)).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun `The message keeps its bounds while the bar steps aside and comes back`() {
        showStorageFull()
        val before = message()
        rule.onNode(hasScrollAction()).performScrollToNode(hasSetTextAction())
        rule.onNode(hasSetTextAction()).requestFocus()
        rule.waitForIdle()

        setKeyboard(KEYBOARD_TOP_360_PX)
        assertFalse("the bar did not step aside", backInTree())
        assertEquals("the message moved with the bar aside", before, message())

        setKeyboard(null)
        assertTrue("the bar did not come back", backInTree())
        assertEquals("the message moved after the keyboard", before, message())
    }
}
