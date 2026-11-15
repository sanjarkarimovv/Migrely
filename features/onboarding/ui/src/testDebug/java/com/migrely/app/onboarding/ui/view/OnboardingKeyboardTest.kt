package com.migrely.app.onboarding.ui.view

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val PROBE = "probe"

/**
 * Onboarding has no text field, so it ignores the keyboard (BRD D180 (3), ux F2, #145): a 250 dp keyboard inset leaves
 * its step's area as it was, at 200% in landscape, where the step scrolls.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w891dp-h411dp")
class OnboardingKeyboardTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `A keyboard inset leaves the step as it was`() {
        rule.runOnUiThread { rule.activity.drawEdgeToEdge() }
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                MigrelyTheme {
                    OnboardingContents(
                        step = 0,
                        actions = OnboardingActions({}, {}, {}, {}, {}),
                        reminderOffer = {},
                        messages = {}
                    )
                    // A control: this box does pad for the keyboard, so it shows the inset arrived.
                    Box(
                        Modifier
                            .fillMaxSize()
                            .imePadding()
                            .testTag(PROBE)
                    )
                }
            }
        }
        rule.waitForIdle()
        val before = rule.onNode(hasScrollAction()).fetchSemanticsNode().boundsInRoot

        rule.runOnUiThread { rule.activity.setKeyboard(KEYBOARD_DP) }
        rule.waitForIdle()

        val probe = rule.onNodeWithTag(PROBE).getUnclippedBoundsInRoot()
        assertEquals("no keyboard arrived", 411f - KEYBOARD_DP, probe.bottom.value, 0.5f)
        assertEquals(before, rule.onNode(hasScrollAction()).fetchSemanticsNode().boundsInRoot)
    }
}
