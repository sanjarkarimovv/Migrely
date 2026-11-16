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

/**
 * Legal's final wording (BRD D181 (1), `reports/2026-11-15-legal-t66-wording.md`), written here
 * and not read from `R.string`: a test that reads the resource it checks cannot fail on a drift.
 */
private val LEGAL_TEXT = listOf(
    "Migrely has no internet permission, so the app itself cannot send anything over the " +
        "internet. There's no account to make and nothing to sign in to.",
    "Premium is bought through Google Play, which goes online for the purchase.",
    "Your entries stay on this phone. If Android's own backup is on, it may also copy them to " +
        "your Google account. On Android 9 and later, it does that only when it can encrypt the " +
        "copy end to end, which needs a screen lock. On Android 8, Migrely cannot require that, " +
        "so the copy may not be end-to-end encrypted. Android can also move your entries to a " +
        "new phone when you set it up.",
    "Apart from Android's own backup and moving to a new phone, your entries leave the phone " +
        "only in a file you export yourself: a backup, a spreadsheet or a report for your " +
        "doctor. That file goes wherever you send it."
)
private const val STOPS = 7
private const val POLICY_LINK = "Read the full privacy policy"
private const val POLICY_ADDRESS = "migrely.health/privacy"

/**
 * The Privacy page reads 7 stops below its title: legal's four paragraphs first, the policy link
 * with its address last (BRD D181 (1), EC-STO-11, EC-STO-12).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h640dp")
class PrivacyContentsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `When shown then the page reads 7 stops, legal's text first and the policy link last`() {
        // When
        composeTestRule.setContent { MigrelyTheme { PrivacyContents(onBack = {}) } }

        // Then
        val stops = composeTestRule
            .onAllNodes(
                SemanticsMatcher.keyIsDefined(SemanticsProperties.Text) and !isHeading()
            )
            .fetchSemanticsNodes()
            .map { node ->
                node.config.getOrNull(SemanticsProperties.Text).orEmpty().map { it.text }
            }
        assertEquals(STOPS, stops.size)
        assertEquals(LEGAL_TEXT, stops.take(LEGAL_TEXT.size).map { it.joinToString("") })
        assertEquals(listOf(POLICY_LINK, POLICY_ADDRESS), stops.last())
    }
}
