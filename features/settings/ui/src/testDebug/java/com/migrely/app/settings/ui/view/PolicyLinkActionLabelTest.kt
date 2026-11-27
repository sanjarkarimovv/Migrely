package com.migrely.app.settings.ui.view

import android.app.Application
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performScrollTo
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The policy link's click label is an action of its own, not the link's visible words: TalkBack
 * says "Double-tap to %s", in German "Zum %s doppeltippen" (T23, ux T72 verdict C). Written out
 * here and not read from `R.string`, so a drift fails.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class PolicyLinkActionLabelTest {
    @get:Rule
    val rule = createComposeRule()

    /** The click label of the link named [shownWords]: its name stays the visible words. */
    private fun clickLabel(shownWords: String): String? {
        rule.setContent { MigrelyTheme { PrivacyContents(onBack = {}) } }
        return rule
            .onNode(
                hasContentDescription(shownWords) and
                    SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)
            )
            .performScrollTo()
            .fetchSemanticsNode()
            .config[SemanticsActions.OnClick].label
    }

    @Test
    @Config(qualifiers = "de")
    fun `Given German then TalkBack's hint names the action, not the link's words`() {
        assertEquals(
            "Lesen der ganzen Datenschutzerklärung",
            clickLabel("Ganze Datenschutzerklärung lesen (auf Englisch)")
        )
    }

    @Test
    fun `Given English then the hint says what it said before`() {
        assertEquals("read the full privacy policy", clickLabel("Read the full privacy policy"))
    }
}
