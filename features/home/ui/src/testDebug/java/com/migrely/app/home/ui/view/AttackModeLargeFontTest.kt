package com.migrely.app.home.ui.view

import android.app.Application
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.migrely.app.designsystem.theme.MigrelyTheme
import com.migrely.app.home.ui.model.AttackInProgressUiModel
import com.migrely.app.home.ui.model.IntensityLevelUiModel
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val LARGEST_FONT = 2f

/**
 * Attack mode at the largest font on a small phone in portrait keeps its guidance reachable: the 72-hour card with its
 * urgent advice (BRD D171), the calming line, the start time and Details (#274). The controls alone can fill the
 * height there, so the guidance must scroll with them instead of getting no height at all.
 *
 * The window is 360 x 582 dp: a 360 x 640 dp phone less its 34 dp status bar and 24 dp gesture bar, as measured on
 * the API 36 emulator where #274 was seen (Robolectric draws no system bars).
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = Application::class)
class AttackModeLargeFontTest {
    @get:Rule
    val rule = createComposeRule()

    private fun showAttackModeWithTheLongCheckDue() {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, LARGEST_FONT)) {
                MigrelyTheme {
                    AttackModeContents(
                        attack = AttackInProgressUiModel(
                            id = "a",
                            startEpochMillis = 0L,
                            startZoneId = "Europe/Berlin",
                            currentLevel = IntensityLevelUiModel.Moderate,
                            longAttackCheckDue = true
                        ),
                        nowMillis = 73L * 60 * 60 * 1000,
                        reduceMotion = true,
                        messageBottom = { 0.dp },
                        timeUiFormatter = previewFormatter,
                        actions = previewActions
                    )
                }
            }
        }
    }

    /** Each text can be brought on screen, by scrolling where it scrolls, and then shows whole, not cut. */
    private fun assertReachable(vararg texts: String) {
        texts.forEach { text ->
            val node = rule.onNode(hasText(text, substring = true) or hasContentDescription(text))
            if (hasScrollingAncestor(node.fetchSemanticsNode())) node.performScrollTo()
            val shown = node.fetchSemanticsNode()
            assertTrue(
                "\"$text\" shows ${shown.boundsInRoot.height} of ${shown.size.height} px",
                shown.size.height > 0 && shown.boundsInRoot.height >= shown.size.height - 1f
            )
        }
    }

    private fun hasScrollingAncestor(node: SemanticsNode): Boolean =
        generateSequence(node.parent) { it.parent }
            .any { SemanticsActions.ScrollBy in it.config }

    @Test
    @Config(qualifiers = "de-rDE-w360dp-h582dp-port-xxhdpi")
    fun `Given German at 200 percent on a small phone then the 72 hour card is reachable`() {
        showAttackModeWithTheLongCheckDue()

        assertReachable(
            "Hält sie noch an?",
            "Wenn sie nach 72 Stunden noch anhält, hol dir dringend ärztlichen Rat.",
            "Ruh dich aus.",
            "Details zu dieser Migräne",
            "Migräne vorbei"
        )
    }

    @Test
    @Config(qualifiers = "en-rUS-w360dp-h582dp-port-xxhdpi")
    fun `Given English at 200 percent on a small phone then the 72 hour card is reachable`() {
        showAttackModeWithTheLongCheckDue()

        assertReachable(
            "Still going?",
            "If it's still going after 72 hours, get urgent medical advice.",
            "Rest now.",
            "Details of this migraine",
            "Migraine ended"
        )
    }
}
