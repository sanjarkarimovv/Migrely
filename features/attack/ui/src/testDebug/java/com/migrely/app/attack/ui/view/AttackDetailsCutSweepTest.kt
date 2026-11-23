package com.migrely.app.attack.ui.view

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.DpSize
import com.migrely.app.attack.ui.model.AttackDetailsViewStateUiModel
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.test.talkback.cutSweep
import com.migrely.app.test.talkback.turnOnTalkBack
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** The symptom, trigger and location chips: selectable, with no role; the impact choices are radio buttons. */
private val choiceChips =
    SemanticsMatcher.keyIsDefined(SemanticsProperties.Selected) and
        !SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton)

/**
 * Every stop on Attack details says its name when the visible edge cuts it (#255, design D4.2 and D7). With exact
 * bounds a chip's text child leaves the tree while the chip itself stays a stop, so the chip carries its words on its
 * own node. This harness gives the whole screen exact bounds, as prototype 2 did, to check the names alone; the page's
 * own wiring through `PageScroll` is checked by the order and scroll tests. Other stops that lose their name when cut
 * (buttons, impact choices, the note) are task 4.1's list.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class AttackDetailsCutSweepTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private fun showWithExactBounds() {
        rule.runOnUiThread { rule.activity.turnOnTalkBack() }
        rule.setContent {
            val around = LocalViewConfiguration.current
            val exact = object : ViewConfiguration by around {
                override val minimumTouchTargetSize = DpSize.Zero
            }
            CompositionLocalProvider(LocalViewConfiguration provides exact) {
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
    }

    @Test
    @Config(sdk = [35], qualifiers = "w411dp-h891dp")
    fun `Portrait at 100 percent, every chip cut at an edge says its name`() {
        showWithExactBounds()
        assertEquals(emptyList<String>(), rule.cutSweep(of = choiceChips))
    }
}
