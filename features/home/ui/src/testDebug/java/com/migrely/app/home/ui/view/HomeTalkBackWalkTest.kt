package com.migrely.app.home.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.home.ui.model.AttackInProgressUiModel
import com.migrely.app.test.talkback.talkBackWalk
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * TalkBack walks every page of this module end to end, each stop once and named, also when cut by an edge (#255,
 * tasks 4.x): its order down each page, its scroll back and on, and the cut sweep. A window drawn edge to edge with
 * a navigation bar, so each page's visible part ends above the window, as on a phone.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class HomeTalkBackWalkTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `Portrait at 100 percent, TalkBack walks every page`() =
        assertEquals(emptyList<String>(), rule.talkBackWalk(1f, homeScreens))

    @Test
    @Config(sdk = [35], qualifiers = "w360dp-h606dp-port-xxhdpi")
    fun `Portrait 360 dp at 200 percent, TalkBack walks attack mode with the 72 hour card`() =
        assertEquals(emptyList<String>(), rule.talkBackWalk(2f, attackModeWithLongCheck))

    @Test
    @Config(sdk = [35], qualifiers = "w800dp-h360dp")
    fun `Landscape 360 dp tall at 200 percent, TalkBack walks every page`() =
        assertEquals(emptyList<String>(), rule.talkBackWalk(2f, homeScreens))
}

// The 360 x 640 dp phone of #274 less its 34 dp status bar; the walk adds the 24 dp navigation bar.
private val attackModeWithLongCheck: Map<String, @Composable () -> Unit> = mapOf(
    "Attack mode with the 72-hour card" to {
        MigrelyTheme {
            AttackModeContents(
                attack = AttackInProgressUiModel("a", 0L, "Europe/Berlin", null, true),
                nowMillis = 73L * 60 * 60 * 1000,
                reduceMotion = true,
                messageBottom = { 0.dp },
                timeUiFormatter = previewFormatter,
                actions = previewActions
            )
        }
    }
)
