package com.migrely.app.history.ui.view

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.history.ui.model.HistoryAttackUiModel
import com.migrely.app.history.ui.model.HistoryViewStateUiModel
import com.migrely.app.test.talkback.TOUCH_BAR_DP
import com.migrely.app.test.talkback.auditTouchTargets
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

// A calendar day ("Thursday, 1 October: …", today "Today, Friday, 23 October: …"): its bar is its whole cell pitch
// (design D5 T1), 47.7 dp in a 328 dp row, as seven 48 dp targets do not fit there; less a pixel, as a row splits into
// whole pixels.
private val DAY = Regex("""^(Today, )?\p{L}+, \d+ \p{L}+:""")
private const val DAY_PITCH_BAR_DP = 47f

/**
 * Touch-target audit of History before exact bounds land on them (#255, design D5; lead decision 2026-11-19, option
 * 1): every control inside a page is 48 dp of its own, at 360 and 411 dp wide and in
 * landscape, at 100% and 200% font.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HistoryTouchTargetAuditTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun underBar(fontScale: Float) = rule.auditTouchTargets(fontScale, historyScreens) {
        if (DAY.containsMatchIn(it.words)) DAY_PITCH_BAR_DP else TOUCH_BAR_DP
    }

    @Test
    @Config(sdk = [35], application = Application::class, qualifiers = "w360dp-h800dp")
    fun `360 dp, 100 percent`() = assertEquals(emptyList<String>(), underBar(1f))

    @Test
    @Config(sdk = [35], application = Application::class, qualifiers = "w411dp-h891dp")
    fun `411 dp, 100 percent`() = assertEquals(emptyList<String>(), underBar(1f))

    @Test
    @Config(sdk = [35], application = Application::class, qualifiers = "w360dp-h800dp")
    fun `360 dp, 200 percent`() = assertEquals(emptyList<String>(), underBar(2f))

    @Test
    @Config(sdk = [35], application = Application::class, qualifiers = "w411dp-h891dp")
    fun `411 dp, 200 percent`() = assertEquals(emptyList<String>(), underBar(2f))

    // Landscape draws the two-pane layout, whose own scrolls hold these controls.
    @Test
    @Config(sdk = [35], application = Application::class, qualifiers = "w800dp-h360dp")
    fun `Landscape 360 dp tall, 100 percent`() = assertEquals(emptyList<String>(), underBar(1f))

    @Test
    @Config(sdk = [35], application = Application::class, qualifiers = "w800dp-h360dp")
    fun `Landscape 360 dp tall, 200 percent`() = assertEquals(emptyList<String>(), underBar(2f))
}
