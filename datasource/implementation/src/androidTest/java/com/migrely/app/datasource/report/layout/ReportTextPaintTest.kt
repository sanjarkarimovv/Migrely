package com.migrely.app.datasource.report.layout

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/** The report's text keeps the font's own advances on a PDF page, whose unit is a point (store polish). */
@RunWith(AndroidJUnit4::class)
class ReportTextPaintTest {
    @Test
    fun givenBodyText_whenMeasured_thenItsAdvancesScaleWithTheSize_unroundedToWholePoints() {
        val body = reportTextPaint(10f, bold = false, color = Color.BLACK)
        val large = reportTextPaint(100f, bold = false, color = Color.BLACK)

        // Hinted, a 10 pt space in Roboto measured 2 against 2.5 from 100 pt.
        listOf(" ", "a month", "at the").forEach {
            assertEquals(it, large.measureText(it) / 10, body.measureText(it), 0.02f)
        }
    }
}
