package com.migrely.app.datasource.report.layout

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.datasource.report.datasource.ReportDocumentDataSourceImpl
import com.migrely.app.datasource.report.model.ReportBlockDataModel
import com.migrely.app.datasource.report.model.ReportColumnDataModel
import com.migrely.app.datasource.report.model.ReportDocumentDataModel
import com.migrely.app.datasource.report.model.ReportPaperDataModel
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val DISCLAIMER = "Patient-entered data. Not a diagnosis."

/** The final approved text (design "Final copy"); a longer one must fail here, not on the doctor's page. */
private const val CREDIT =
    "Made with Migrely, a personal migraine diary app. Not a medical device. migrely.health"
private const val MARGIN = 40
private const val SCALE = 2

/**
 * The credit line on the doctor report (BRD REP-F5, D186, design D2, tasks 4.2 and 4.3). The pixel checks compare the
 * same report drawn with the credit on and off, so they see what `PdfDocument` really drew.
 */
@RunWith(AndroidJUnit4::class)
class ReportCreditLayoutTest {
    private val cache = File(
        ApplicationProvider.getApplicationContext<android.content.Context>().cacheDir,
        "report-credit-test"
    )
    private val written = mutableListOf<File>()
    private val dataSource = ReportDocumentDataSourceImpl(cache) { file ->
        written += file
        "content://test/${file.name}"
    }

    @Before
    fun setUp() {
        cache.deleteRecursively()
    }

    private fun document(paper: ReportPaperDataModel, credit: String?, rows: Int = 150) =
        ReportDocumentDataModel(
            fileName = "migrely-report-2026-11-16.pdf",
            paper = paper,
            footer = DISCLAIMER,
            pageLabel = "Page %1\$d of %2\$d",
            maxPages = 50,
            credit = credit,
            blocks = listOf(
                ReportBlockDataModel.Title("Migraine diary report"),
                ReportBlockDataModel.Table(
                    columns = listOf(
                        ReportColumnDataModel("Start", 2f),
                        ReportColumnDataModel("Note", 3f)
                    ),
                    rows = List(rows) { listOf("2026-10-${it % 28 + 1} 09:00", "Row $it") }
                )
            )
        )

    @Test
    fun givenA4AndLetter_thenTheCreditIsOneLineWithNoEllipsis() {
        ReportPaperDataModel.entries.forEach { paper ->
            val layout = ReportPageLayout(document(paper, CREDIT)).creditLayout(CREDIT)

            assertEquals("$paper lines", 1, layout.lineCount)
            assertEquals("$paper ellipsis", 0, layout.getEllipsisCount(0))
            assertTrue("$paper width", layout.getLineWidth(0) <= paper.width - 2 * MARGIN)
        }
    }

    @Test
    fun givenAMultiPageReport_thenOnlyTheLastPageDiffers_andOnlyBelowTheDisclaimer() {
        ReportPaperDataModel.entries.forEach { paper ->
            val on = pages(document(paper, CREDIT))
            val off = pages(document(paper, null))
            // The rendered page counts: the credit sits in the bottom margin and never adds a page (EC-REP-06).
            assertEquals("$paper pages", off.size, on.size)
            assertTrue("$paper must be several pages", on.size > 1)

            on.dropLast(1).zip(off.dropLast(1)).forEachIndexed { index, (withCredit, without) ->
                assertTrue("$paper page ${index + 1} changed", withCredit.sameAs(without))
            }
            val changed = changedRows(on.last(), off.last())
            assertFalse("$paper: the last page shows no credit", changed.isEmpty())
            // Line 1 starts 54 pt above the edge; the credit is below it and inside the 40 pt margin's band.
            val heightPt = paper.height
            assertTrue(
                "$paper: credit drawn above line 1",
                changed.min() >= (heightPt - 50) * SCALE
            )
            assertTrue("$paper: credit drawn too low", changed.max() <= (heightPt - 30) * SCALE)
        }
    }

    @Test
    fun givenTheSwitchOff_thenEveryPageStillHasTheDisclaimer() {
        // medical-reviewer's condition: the disclaimer's band is drawn on every page with the credit off.
        ReportPaperDataModel.entries.forEach { paper ->
            val off = pages(document(paper, null))
            val blank = pages(document(paper, null).copy(footer = " "))

            off.zip(blank).forEachIndexed { index, (withDisclaimer, without) ->
                val rows = changedRows(withDisclaimer, without)
                assertFalse("$paper page ${index + 1} has no disclaimer", rows.isEmpty())
                assertTrue(
                    "$paper page ${index + 1}: disclaimer out of place",
                    rows.min() >= (paper.height - 56) * SCALE &&
                        rows.max() <= (paper.height - 40) * SCALE
                )
            }
        }
    }

    private fun pages(document: ReportDocumentDataModel): List<Bitmap> {
        written.clear()
        dataSource.write(document)
        val file = written.single()
        return PdfRenderer(
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        ).use { pdf ->
            List(pdf.pageCount) { index ->
                pdf.openPage(index).use { page ->
                    Bitmap.createBitmap(
                        page.width * SCALE,
                        page.height * SCALE,
                        Bitmap.Config.ARGB_8888
                    )
                        .apply {
                            eraseColor(Color.WHITE)
                            page.render(this, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        }
                }
            }
        }
    }

    /** The pixel rows where [a] and [b] differ. */
    private fun changedRows(a: Bitmap, b: Bitmap): List<Int> {
        val width = a.width
        val rowA = IntArray(width)
        val rowB = IntArray(width)
        return (0 until a.height).filter { y ->
            a.getPixels(rowA, 0, width, 0, y, width, 1)
            b.getPixels(rowB, 0, width, 0, y, width, 1)
            !rowA.contentEquals(rowB)
        }
    }
}
