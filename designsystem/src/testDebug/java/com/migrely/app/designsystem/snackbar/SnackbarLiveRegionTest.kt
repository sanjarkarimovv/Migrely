package com.migrely.app.designsystem.snackbar

import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createComposeRule
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** How TalkBack is told about a message: errors at once, unless the screen asks for polite (BRD EC-UX-04, D165 (3)). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SnackbarLiveRegionTest {
    @get:Rule
    val rule = createComposeRule()

    private fun liveRegionOf(
        tone: SnackbarTone,
        announcePolitely: Boolean? = null
    ): LiveRegionMode {
        rule.setContent {
            MigrelyTheme {
                if (announcePolitely == null) {
                    MigrelySnackbar("Text", tone, null, null, "Dismiss", {})
                } else {
                    MigrelySnackbar(
                        "Text",
                        tone,
                        null,
                        null,
                        "Dismiss",
                        {},
                        announcePolitely = announcePolitely
                    )
                }
            }
        }
        return rule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion))
            .fetchSemanticsNode().config[SemanticsProperties.LiveRegion]
    }

    @Test
    fun `An error is assertive by default`() {
        assertEquals(LiveRegionMode.Assertive, liveRegionOf(SnackbarTone.Error))
    }

    @Test
    fun `An error is polite when the screen asks`() {
        assertEquals(
            LiveRegionMode.Polite,
            liveRegionOf(SnackbarTone.Error, announcePolitely = true)
        )
    }

    @Test
    fun `An error asked not to be polite stays assertive`() {
        assertEquals(
            LiveRegionMode.Assertive,
            liveRegionOf(SnackbarTone.Error, announcePolitely = false)
        )
    }

    @Test
    fun `A confirmation is polite`() {
        assertEquals(LiveRegionMode.Polite, liveRegionOf(SnackbarTone.Confirmation))
    }
}
