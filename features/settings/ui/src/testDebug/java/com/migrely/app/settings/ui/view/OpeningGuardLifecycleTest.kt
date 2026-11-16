package com.migrely.app.settings.ui.view

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.migrely.app.designsystem.theme.MigrelyTheme
import java.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowSystemClock

private const val SHARE = "Share Migrely"

/**
 * Settings' pause and resume reach the Share and Rate guard (`rememberOpeningGuard`, BRD EC-SHR-05, design D1): after
 * the share sheet, Settings answers again once it is back, and not before.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class OpeningGuardLifecycleTest {
    @get:Rule
    val rule = createComposeRule()

    private val owner = object : LifecycleOwner {
        val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = registry
    }
    private var shares = 0

    private fun show() {
        rule.runOnUiThread { owner.registry.currentState = Lifecycle.State.RESUMED }
        rule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                MigrelyTheme {
                    // The default guard: the one Settings builds with rememberOpeningGuard().
                    SettingsContents(
                        settings = PREVIEW_SETTINGS,
                        actions = PREVIEW_SETTINGS_ACTIONS.copy(onShare = { shares++ }),
                        messages = {}
                    )
                }
            }
        }
    }

    private fun move(to: Lifecycle.State) {
        rule.runOnUiThread { owner.registry.currentState = to }
        rule.waitForIdle()
    }

    private fun tapShare() = rule.onNodeWithText(SHARE, substring = true).performScrollTo().performClick()

    @Test
    fun `Given Settings came back from the share sheet then Share opens again at once`() {
        show()
        tapShare()

        move(Lifecycle.State.STARTED)
        move(Lifecycle.State.RESUMED)
        tapShare()

        assertEquals(2, shares)
    }

    @Test
    fun `While Settings is away for the share sheet a tap opens nothing, however late`() {
        show()
        tapShare()

        move(Lifecycle.State.STARTED)
        // Past the double-tap timeout, so only the pause can hold it.
        ShadowSystemClock.advanceBy(Duration.ofSeconds(5))
        tapShare()

        assertEquals(1, shares)
    }
}
