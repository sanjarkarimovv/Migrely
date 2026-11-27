package com.migrely.app.settings.ui.view

import android.app.Application
import com.migrely.app.settings.ui.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Word for word from `specs/app-settings/spec.md`, medical-reviewer PASS 2026-11-27. */
private const val SUMMARY_EN =
    "When to call your local emergency number and when to get urgent medical advice"
private const val SUMMARY_DE = "Wann den Notruf wählen, wann dringend ärztlichen Rat holen"

/**
 * The summary under "Warning signs" names both actions of the page in the page's words, never
 * "care" and never the two tiers joined by "or", which reads as a choice (BRD D171, EC-SAF-18).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class WarningSignsSummaryTest {
    private val summary
        get() = RuntimeEnvironment.getApplication().resources
            .getString(R.string.settings_warning_signs_summary)

    @Test
    fun `In English the summary names both tiers, without care or or`() {
        assertEquals(SUMMARY_EN, summary)
        assertFalse(summary, summary.contains("care", ignoreCase = true))
        assertFalse(summary, summary.contains(" or ", ignoreCase = true))
    }

    @Test
    @Config(qualifiers = "de")
    fun `In German the summary names both tiers, without oder, Hilfe or a number`() {
        assertEquals(SUMMARY_DE, summary)
        assertFalse(summary, summary.contains("oder", ignoreCase = true))
        assertFalse(summary, summary.contains("Hilfe", ignoreCase = true))
        assertFalse(summary, summary.any(Char::isDigit))
    }
}
