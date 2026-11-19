package com.migrely.app.attack.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.test.talkback.touchTargets
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val BAR_DP = 48

/** Touch-target audit of Attack details before exact bounds land on it (#255, design D5). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AttackTouchTargetAuditTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun underBar(fontScale: Float): List<String> {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
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
                        actions = previewActions
                    )
                }
            }
        }
        val all = rule.touchTargets()
        println("AUDIT ${all.size} controls: " + all.joinToString("; "))
        return all.filter { it.widthDp < BAR_DP || it.heightDp < BAR_DP }.map { it.toString() }
    }

    @Test
    @Config(sdk = [35], qualifiers = "w360dp-h800dp")
    fun `Attack details at 360 dp, 100 percent`() = assertEquals(emptyList<String>(), underBar(1f))

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `Attack details at 411 dp, 100 percent`() = assertEquals(emptyList<String>(), underBar(1f))

    @Test
    @Config(sdk = [35], qualifiers = "w360dp-h800dp")
    fun `Attack details at 360 dp, 200 percent`() = assertEquals(emptyList<String>(), underBar(2f))

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `Attack details at 411 dp, 200 percent`() = assertEquals(emptyList<String>(), underBar(2f))
}
