package com.migrely.app.attack.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
import com.migrely.app.attack.ui.model.PastAttackDraftUiModel
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.test.talkback.auditTouchTargets
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val NOW = 1_792_829_200_000L

/**
 * Touch-target audit of the attack pages before exact bounds land on them (#255, design D5; lead decision 2026-11-19,
 * option 1): every control inside a page is 48 dp or more of its own.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AttackTouchTargetAuditTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val screens = mapOf(
        "Attack details" to @Composable {
            MigrelyTheme {
                AttackDetailsContents(
                    viewState = AttackDetailsViewStateUiModel.Ready(previewDetails()),
                    timeUiFormatter = previewTimeFormatter,
                    labels = AttackLabels.Default,
                    localState = AttackDetailsLocalState(
                        timeError = null,
                        safetyCardVisible = true,
                        onDismissSafetyCard = {},
                        onTimeErrorShown = {}
                    ),
                    actions = previewActions
                )
            }
        },
        "Add past attack" to @Composable {
            MigrelyTheme {
                AddPastAttackContents(
                    draft = PastAttackDraftUiModel(NOW - 7_200_000L, NOW, "UTC"),
                    problem = null,
                    saving = false,
                    timeUiFormatter = previewTimeFormatter,
                    actions = AddPastAttackActions({}, {}, {}, {}, {}, {}),
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
