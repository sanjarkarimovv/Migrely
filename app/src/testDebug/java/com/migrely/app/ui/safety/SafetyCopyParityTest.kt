package com.migrely.app.ui.safety

import android.app.Application
import com.migrely.app.attack.ui.R as AttackR
import com.migrely.app.settings.ui.R as SettingsR
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

private val OLD_WORDS = listOf("worst", "emergency services")

/**
 * The first-10 safety card and the Warning signs page name the same sign in the same words
 * (BRD SAF-F1, SAF-F2, D173 (1), EC-SAF-11). The two string sets live in features that do not
 * depend on each other, so only `:app` can compare them.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class SafetyCopyParityTest {
    private val resources = RuntimeEnvironment.getApplication().resources

    private val cardTitle get() = resources.getString(AttackR.string.safety_title)
    private val cardSentence get() = resources.getString(AttackR.string.safety_body)
    private val firstSign get() =
        resources.getStringArray(SettingsR.array.settings_warning_signs_list).first()

    @Test
    fun `The card's sentence contains the page's first emergency sign`() {
        // Given
        val sign = firstSign.lowercase()

        // Then
        assertTrue("\"$cardSentence\" should contain \"$sign\"", cardSentence.contains(sign))
    }

    @Test
    fun `Neither card string carries the old words`() {
        // Given
        val cardStrings = listOf(cardTitle, cardSentence)

        // Then
        val found = cardStrings.flatMap { text ->
            OLD_WORDS.filter { text.contains(it, ignoreCase = true) }.map { "\"$it\" in \"$text\"" }
        }
        assertEquals(emptyList<String>(), found)
    }
}
