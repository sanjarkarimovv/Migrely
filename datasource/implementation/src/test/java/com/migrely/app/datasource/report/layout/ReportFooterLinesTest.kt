package com.migrely.app.datasource.report.layout

import org.junit.Assert.assertEquals
import org.junit.Test

private const val DISCLAIMER = "Patient-entered data. Not a diagnosis."
private const val CREDIT =
    "Made with Migrely, a personal migraine diary app. Not a medical device. migrely.health"

/** Which footer lines each page draws (BRD REP-F5, D186, EC-REP-05–07). */
class ReportFooterLinesTest {
    @Test
    fun `Given the switch off then every page of a multi-page report has the disclaimer and no credit`() {
        // medical-reviewer's condition: the disclaimer never depends on the credit switch.
        val pages = 7

        (1..pages).forEach { page ->
            assertEquals(listOf(DISCLAIMER), footerLines(DISCLAIMER, null, page, pages))
        }
    }

    @Test
    fun `Given the switch on then only the last page adds the credit, under the disclaimer`() {
        val pages = 7

        (1 until pages).forEach { page ->
            assertEquals(listOf(DISCLAIMER), footerLines(DISCLAIMER, CREDIT, page, pages))
        }
        assertEquals(listOf(DISCLAIMER, CREDIT), footerLines(DISCLAIMER, CREDIT, pages, pages))
    }

    @Test
    fun `Given one page then that page has both lines`() {
        assertEquals(listOf(DISCLAIMER, CREDIT), footerLines(DISCLAIMER, CREDIT, 1, 1))
    }

    @Test
    fun `Given a report cut at the page limit then the last page drawn has the credit`() {
        // render() takes the first maxPages pages and draws each with the count it kept (EC-REP-02).
        val drawn = 50

        assertEquals(listOf(DISCLAIMER, CREDIT), footerLines(DISCLAIMER, CREDIT, drawn, drawn))
    }
}
