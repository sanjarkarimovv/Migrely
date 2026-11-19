package com.migrely.app.subscription.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.subscription.ui.model.PaywallContextUiModel
import com.migrely.app.subscription.ui.model.PaywallViewStateUiModel
import com.migrely.app.test.talkback.auditTouchTargets
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Touch-target audit of the Paywall and Today's trial banner before exact bounds land on them (#255, design D5; lead decision 2026-11-19, option
 * 1): every control inside a page is 48 dp of its own, at 360 and 411 dp wide and 100% and 200% font.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SubscriptionTouchTargetAuditTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val screens: Map<String, @Composable () -> Unit> = mapOf(
        "Paywall" to {
            MigrelyTheme {
                PaywallContents(
                    context = PaywallContextUiModel.Report,
                    viewState = PaywallViewStateUiModel.Ready(PREVIEW_OFFERS),
                    chosen = null,
                    unlocked = null,
                    actions = PaywallActions({}, {}, {}, {}, {}, {}, {}),
                    messages = {}
                )
            }
        },
        "Today's trial banner" to {
            MigrelyTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    TrialBannerContents(
                        endsToday = true,
                        renews = true,
                        offlineNote = null,
                        onManage = {},
                        onDismiss = {}
                    )
                }
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
