package com.migrely.app.settings.ui.view

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.createComposeRule
import com.migrely.app.designsystem.theme.MigrelyTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private const val TITLE = "Warning signs"
private const val BULLET_GLYPH = "•"

/** The emergency part (BRD SAF-F1, D171), word for word from `specs/app-settings/spec.md`. */
private const val CALL_HEADING =
    "Call your local emergency number if a headache comes with any of these:"
private val EMERGENCY_SIGNS = listOf(
    "Extremely bad pain that came on suddenly",
    "Weakness or loss of movement on one side of your body or face",
    "Problems speaking or remembering things",
    "Losing your vision, or blurred or double vision",
    "Feeling drowsy or confused",
    "A seizure or fit",
    "A very high temperature and signs of meningitis, such as a stiff neck",
    "A rash that doesn't fade when you press a glass against it",
    "A head injury in the last 3 months"
)
private const val NO_DRIVE =
    "Don't drive yourself to hospital. Call your local emergency number."

/** The urgent part (BRD D171). */
private const val URGENT_HEADING = "Get urgent medical advice if you have a migraine attack and:"
private val URGENT_SIGNS = listOf(
    "It has lasted longer than 72 hours",
    "Aura symptoms last longer than 1 hour at a time",
    "You're pregnant or have just had a baby"
)
private const val OTHER =
    "If you have any other symptoms you're worried about, get urgent medical advice."

private const val ABOUT_URGENT = "If a headache comes with any of the emergency signs on the " +
    "Warning signs page, call your local emergency number."

/**
 * The Warning signs page reads the spec's text in the spec's order, with the two part headings as
 * accessibility headings and no bullet glyph exposed (BRD SAF-F1, D171; design D5).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w320dp-h640dp")
class WarningSignsContentsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `When shown then every text reads in the spec's order`() {
        // Given
        val expected = listOf(TITLE, CALL_HEADING) + EMERGENCY_SIGNS + NO_DRIVE +
            URGENT_HEADING + URGENT_SIGNS + OTHER

        // When
        composeTestRule.setContent { MigrelyTheme { WarningSignsContents(onBack = {}) } }

        // Then
        assertEquals(expected, texts(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text)))
    }

    @Test
    fun `When shown then the title and the two part headings are the only headings`() {
        // When
        composeTestRule.setContent { MigrelyTheme { WarningSignsContents(onBack = {}) } }

        // Then
        assertEquals(listOf(TITLE, CALL_HEADING, URGENT_HEADING), texts(isHeading()))
    }

    @Test
    fun `When shown then no bullet glyph is exposed`() {
        // When
        composeTestRule.setContent { MigrelyTheme { WarningSignsContents(onBack = {}) } }

        // Then
        val glyphs = texts(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text))
            .filter { it.contains(BULLET_GLYPH) }
        assertEquals(emptyList<String>(), glyphs)
    }

    @Test
    fun `When About is shown then its last paragraph points to the emergency signs only`() {
        // When
        composeTestRule.setContent { MigrelyTheme { AboutMigrelyContents(onBack = {}) } }

        // Then
        assertEquals(
            ABOUT_URGENT,
            texts(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text)).last()
        )
    }

    /** The texts of the matching nodes, in the tree's (screen) order. */
    private fun texts(matcher: SemanticsMatcher): List<String> = composeTestRule
        .onAllNodes(matcher, useUnmergedTree = true)
        .fetchSemanticsNodes()
        .map { node ->
            node.config.getOrNull(SemanticsProperties.Text)
                .orEmpty()
                .joinToString(separator = "") { it.text }
        }
}
