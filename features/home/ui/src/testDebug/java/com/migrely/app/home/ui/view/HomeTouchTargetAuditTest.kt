package com.migrely.app.home.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.home.ui.mapper.SkyAppearanceUiMapper
import com.migrely.app.home.ui.model.AttackInProgressUiModel
import com.migrely.app.home.ui.model.IntensityLevelUiModel
import com.migrely.app.home.ui.model.SkyUiModel
import com.migrely.app.home.ui.model.TodayUiModel
import com.migrely.app.test.talkback.TOUCH_BAR_DP
import com.migrely.app.test.talkback.auditTouchTargets
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val LARGE_BAR_DP = 64f

/**
 * Touch-target audit of Today and attack mode before exact bounds land on them (#255, design D5; lead decision 2026-11-19, option
 * 1): every control inside a page is 48 dp (64 dp for attack mode's intensity choices and "Migraine ended") of its own, at 360 and 411 dp wide and in
 * landscape, at 100% and 200% font.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HomeTouchTargetAuditTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    // Attack mode's dark-room controls (attack-mode spec): the intensity choices and "Migraine ended".
    private val largeControls = setOf("Mild", "Moderate", "Severe", "Migraine ended")

    private fun underBar(fontScale: Float) = rule.auditTouchTargets(fontScale, homeScreens) {
        if (it.words in largeControls) LARGE_BAR_DP else TOUCH_BAR_DP
    }

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

    // Landscape draws the two-pane layout, whose own scrolls hold these controls.
    @Test
    @Config(sdk = [35], qualifiers = "w800dp-h360dp")
    fun `Landscape 360 dp tall, 100 percent`() = assertEquals(emptyList<String>(), underBar(1f))

    @Test
    @Config(sdk = [35], qualifiers = "w800dp-h360dp")
    fun `Landscape 360 dp tall, 200 percent`() = assertEquals(emptyList<String>(), underBar(2f))
}
