package com.migrely.app.onboarding.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.test.talkback.auditTouchTargets
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Touch-target audit of the onboarding steps before exact bounds land on them (#255, design D5; lead decision 2026-11-19, option
 * 1): every control inside a page is 48 dp of its own, at 360 and 411 dp wide and 100% and 200% font.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OnboardingTouchTargetAuditTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val screens: Map<String, @Composable () -> Unit> = mapOf(
        "Onboarding step 0" to {
            MigrelyTheme {
                OnboardingContents(
                    step = 0,
                    actions = OnboardingActions({}, {}, {}, {}, {}),
                    reminderOffer = {},
                    messages = {}
                )
            }
        },
        "Onboarding step 1" to {
            MigrelyTheme {
                OnboardingContents(
                    step = 1,
                    actions = OnboardingActions({}, {}, {}, {}, {}),
                    reminderOffer = {},
                    messages = {}
                )
            }
        },
        "Onboarding step 2" to {
            MigrelyTheme {
                OnboardingContents(
                    step = 2,
                    actions = OnboardingActions({}, {}, {}, {}, {}),
                    reminderOffer = {},
                    messages = {}
                )
            }
        },
        "Onboarding step 3" to {
            MigrelyTheme {
                OnboardingContents(
                    step = 3,
                    actions = OnboardingActions({}, {}, {}, {}, {}),
                    reminderOffer = {},
                    messages = {}
                )
            }
        }
    )

    private fun underBar(fontScale: Float) = rule.auditTouchTargets(fontScale, screens)

    @Test
    @Config(sdk = [35], qualifiers = "w360dp-h800dp")
    fun `360 dp, 100 percent`() = assertEquals(emptyList<String>(), underBar(1f))

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `411 dp, 100 percent`() = assertEquals(emptyList<String>(), underBar(1f))

    @Test
    @Config(sdk = [35], qualifiers = "w360dp-h800dp")
    fun `360 dp, 200 percent`() = assertEquals(emptyList<String>(), underBar(2f))

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `411 dp, 200 percent`() = assertEquals(emptyList<String>(), underBar(2f))
}
