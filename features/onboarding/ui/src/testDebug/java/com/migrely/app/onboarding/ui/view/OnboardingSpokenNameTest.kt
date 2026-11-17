package com.migrely.app.onboarding.ui.view

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val STEPS = 4

/**
 * Skip reads "Skip this step" once on every step, never followed by "Skip" (BRD D181 (2),
 * EC-ONB-07, #217).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class, qualifiers = "w360dp-h640dp")
class OnboardingSpokenNameTest {
    @get:Rule
    val rule = createComposeRule()

    private var step by mutableIntStateOf(0)

    private fun show(fontScale: Float) {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                MigrelyTheme {
                    OnboardingContents(
                        step = step,
                        actions = OnboardingActions({}, {}, {}, {}, {}),
                        reminderOffer = {},
                        messages = {}
                    )
                }
            }
        }
    }

    private fun assertOnEveryStep() = repeat(STEPS) { index ->
        step = index
        rule.waitForIdle()
        rule.assertOneSpokenName(spoken = "Skip this step", visible = "Skip")
    }

    @Test
    fun `Skip has one spoken name on every step`() {
        show(fontScale = 1f)
        assertOnEveryStep()
    }

    @Test
    @Config(qualifiers = "w640dp-h360dp")
    fun `Skip has one spoken name on every step at 200 percent in landscape`() {
        show(fontScale = 2f)
        assertOnEveryStep()
    }
}
