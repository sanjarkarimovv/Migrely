package com.migrely.app.ui.report

import android.app.Application
import com.migrely.app.report.data.mapper.REPORT_CREDIT
import com.migrely.app.report.ui.R as ReportR
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * The credit switch's summary quotes the line the PDF prints, word for word (BRD REP-F5, D186; medical-reviewer W6).
 * The summary is a string of the report's ui module and the line a constant of its data module, which do not see each
 * other, so only `:app` can compare them.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = Application::class)
class ReportCreditSummaryTest {
    private val summary
        get() = RuntimeEnvironment.getApplication().resources
            .getString(ReportR.string.report_credit_switch_summary)

    @Test
    fun `The switch's summary contains the PDF's credit line exactly`() {
        assertTrue(
            "\"$summary\" should quote \"$REPORT_CREDIT\"",
            summary.contains("\"$REPORT_CREDIT\"")
        )
    }

    @Test
    fun `The summary is the approved text, and nothing more`() {
        assertEquals("One small line on the last page: \"$REPORT_CREDIT\"", summary)
    }

    /**
     * The PDF stays English in 1.4.0 (EC-REP-10), so the German summary quotes the English line,
     * in German quotes.
     */
    @Test
    @Config(qualifiers = "de")
    fun `The German summary quotes the PDF's English credit line exactly`() {
        assertTrue(
            "\"$summary\" should quote \"$REPORT_CREDIT\" in „…“",
            summary.contains("„$REPORT_CREDIT“")
        )
    }
}
