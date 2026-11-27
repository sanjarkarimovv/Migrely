package com.migrely.app.attack.ui.view

import android.app.Application
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.isPopup
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
import com.migrely.app.attack.ui.model.AttackTagUiModel
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowUiAutomation

// The user's own symptom in the preview details.
private const val CUSTOM_CHIP = "Jaw pain"
private const val FRAMES = 30

/**
 * While a custom chip's menu is open, a tap where the chip is only closes the menu: nothing is removed and the chip is
 * not toggled (BRD EC-UX-91, #179). The taps go through Robolectric's window routing, as a finger's do, so the menu's
 * window, which takes focus, gets them first; Compose's own touch input would go straight to the chip's window.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class CustomChipMenuTapTest {
    @get:Rule
    val rule = createComposeRule()

    private val toggled = mutableListOf<AttackTagUiModel>()
    private var removals = 0

    @Before
    fun setUp() {
        rule.setContent {
            MigrelyTheme {
                AttackDetailsContents(
                    viewState = AttackDetailsViewStateUiModel.Ready(previewDetails()),
                    timeUiFormatter = previewTimeFormatter,
                    labels = AttackLabels.Default,
                    localState = AttackDetailsLocalState(
                        timeError = null,
                        safetyCardVisible = false,
                        onDismissSafetyCard = {},
                        onTimeErrorShown = {}
                    ),
                    actions = previewActions.copy(
                        onTag = { tag, _ -> toggled += tag },
                        onArchiveCustomTag = { removals++ }
                    )
                )
            }
        }
        rule.waitForIdle()
    }

    private fun chip() = rule.onNode(hasContentDescription(CUSTOM_CHIP)).performScrollTo()

    private fun chipCentreOnScreen(): Offset {
        val node = chip().fetchSemanticsNode()
        return node.positionOnScreen + Offset(node.size.width / 2f, node.size.height / 2f)
    }

    private fun isMenuOpen() = rule.onAllNodes(isPopup(), useUnmergedTree = true)
        .fetchSemanticsNodes().isNotEmpty()

    private fun frames() = repeat(FRAMES) {
        rule.mainClock.advanceTimeByFrame()
        rule.waitForIdle()
    }

    /** A finger's tap at [point] on the screen, to whichever window Android would give it. */
    private fun tapOnScreen(point: Offset) {
        val downTime = SystemClock.uptimeMillis()
        listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP).forEach { action ->
            val event = MotionEvent.obtain(
                downTime,
                SystemClock.uptimeMillis(),
                action,
                point.x,
                point.y,
                0
            )
            event.source = InputDevice.SOURCE_TOUCHSCREEN
            ShadowUiAutomation.injectInputEvent(event)
            event.recycle()
        }
        rule.waitForIdle()
    }

    @Test
    fun `With no menu the same tap toggles the chip`() {
        tapOnScreen(chipCentreOnScreen())

        assertEquals("the tap reached the chip", 1, toggled.size)
    }

    @Test
    fun `A tap on the chip while its menu is open only closes the menu`() {
        val centre = chipCentreOnScreen()
        chip().performTouchInput { longClick() }
        frames()
        assertTrue("the menu opened", isMenuOpen())

        tapOnScreen(centre)
        frames()

        assertTrue("the menu closed", !isMenuOpen())
        assertEquals("nothing was removed", 0, removals)
        assertEquals("the chip was not toggled", emptyList<AttackTagUiModel>(), toggled)
    }
}
