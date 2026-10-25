package com.migrely.app.datasource.report.layout

import android.graphics.Color
import android.text.TextPaint
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

        // Text is drawn by its glyph advances. Hinted, a 10 pt space advanced 2 against 2.48 from 100 pt. Not
        // measureText(), which rounds its total up to a whole unit whatever the paint.
        listOf(" ", "a month", "at the").forEach {
            assertEquals(it, large.advance(it) / 10, body.advance(it), 0.01f)
        }
    }

    private fun TextPaint.advance(text: String) =
        getRunAdvance(text, 0, text.length, 0, text.length, false, text.length)
}
