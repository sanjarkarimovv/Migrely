package com.migrely.app.datasource.report.datasource

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.migrely.app.datasource.report.model.ReportBlockDataModel
import com.migrely.app.datasource.report.model.ReportColumnDataModel
import com.migrely.app.datasource.report.model.ReportDayLevelDataModel
import com.migrely.app.datasource.report.model.ReportDocumentDataModel
import com.migrely.app.datasource.report.model.ReportFactDataModel
import com.migrely.app.datasource.report.model.ReportHeatMonthDataModel
import com.migrely.app.datasource.report.model.ReportLegendDataModel
import com.migrely.app.datasource.report.model.ReportPaperDataModel
import java.io.File
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Reports drawn with `PdfDocument` into the cache (change add-doctor-report). */
@RunWith(AndroidJUnit4::class)
class ReportDocumentDataSourceImplTest {
    private val cache =
        File(
            ApplicationProvider.getApplicationContext<android.content.Context>().cacheDir,
            "report-test"
        )
    private val written = mutableListOf<File>()
    private val classUnderTest = ReportDocumentDataSourceImpl(cache) { file ->
        written += file
        "content://test/${file.name}"
    }

    @Before
    fun setUp() {
        cache.deleteRecursively()
    }

    private fun document(
        rows: Int,
        maxPages: Int = 50,
        paper: ReportPaperDataModel = ReportPaperDataModel.A4
    ) = ReportDocumentDataModel(
        fileName = "migrely-report-2026-10-25.pdf",
        paper = paper,
        footer = "Patient-entered data. Not a diagnosis.",
        pageLabel = "Page %1\$d of %2\$d",
        maxPages = maxPages,
        blocks = listOf(
            ReportBlockDataModel.Title("Migraine diary report"),
            ReportBlockDataModel.Facts(
                listOf(ReportFactDataModel("Migraine days a month", "5.9"))
            ),
            ReportBlockDataModel.Heading("Calendar"),
            ReportBlockDataModel.HeatMap(
                months = List(3) { month ->
                    ReportHeatMonthDataModel(
                        "Month $month",
                        firstWeekday = month,
                        levels = List(31) { ReportDayLevelDataModel.entries[it % 6] }
                    )
                },
                legend = ReportDayLevelDataModel.entries.drop(1).map {
                    ReportLegendDataModel(it, it.name)
                }
            ),
            ReportBlockDataModel.PageBreak,
            ReportBlockDataModel.Heading("Attacks"),
            ReportBlockDataModel.Table(
                columns = listOf(
                    ReportColumnDataModel("Start", 2f),
                    ReportColumnDataModel("Note", 3f)
                ),
                rows = List(rows) {
                    listOf("2026-10-${it % 28 + 1} 09:00", "Ko‘p uxlamadim, «shovqin» $it")
                },
                shortenedNote = "Showing the latest %1\$d of %2\$d attacks."
            )
        )
    )

    private fun pagesOf(file: File): Int =
        PdfRenderer(ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)).use {
            it.pageCount
        }

    @Test
    fun whenWritten_thenAFinishedFileAndItsUri_andNothingPartial() {
        val uri = classUnderTest.write(document(rows = 10))

        val file = written.single()
        assertEquals("content://test/migrely-report-2026-10-25.pdf", uri)
        assertEquals("reports", file.parentFile!!.parentFile!!.name)
        assertTrue(file.length() > 0)
        assertEquals(2, pagesOf(file))
        assertFalse(File(file.parentFile, file.name + ".partial").exists())
    }

    @Test
    fun givenTooManyAttacks_whenWritten_thenTheReportStaysWithinItsPages() {
        classUnderTest.write(document(rows = 3_000, maxPages = 6))

        assertEquals(6, pagesOf(written.single()))
    }

    @Test
    fun givenLetterPaper_whenWritten_thenLetterPages() {
        classUnderTest.write(document(rows = 1, paper = ReportPaperDataModel.Letter))

        PdfRenderer(
            ParcelFileDescriptor.open(written.single(), ParcelFileDescriptor.MODE_READ_ONLY)
        ).use {
            it.openPage(0).use { page -> assertEquals(612 to 792, page.width to page.height) }
        }
    }

    @Test
    fun whenCleaningUp_thenPartialAndDayOldFilesGo_andTodaysStay() {
        val directory = File(cache, "reports").apply { mkdirs() }
        val now = Instant.parse("2026-10-25T10:00:00Z")
        val partial = File(directory, "a.pdf.partial").apply { writeText("x") }
        val old = File(directory, "old.pdf").apply {
            writeText("x")
            setLastModified(now.minusSeconds(90_000).toEpochMilli())
        }
        val fresh = File(directory, "fresh.pdf").apply {
            writeText("x")
            setLastModified(now.minusSeconds(3_600).toEpochMilli())
        }

        classUnderTest.cleanUp(now)

        assertEquals(listOf(false, false, true), listOf(partial, old, fresh).map(File::exists))
    }

    @Test
    fun givenTwoReportsTheSameDay_whenWritten_thenEachHasItsOwnFile() {
        classUnderTest.write(document(rows = 1))
        classUnderTest.write(document(rows = 2))

        assertEquals(2, written.map { it.absolutePath }.toSet().size)
        assertTrue(written.all(File::exists))
    }

    @Test
    fun givenMoreMonthsThanPagesAllow_whenWritten_thenTheLastPagesAreCutAtTheLimit() {
        val months = List(120) { month ->
            ReportHeatMonthDataModel("Month $month", 0, List(30) { ReportDayLevelDataModel.Mild })
        }
        val long = document(rows = 0, maxPages = 3).copy(
            blocks = listOf(ReportBlockDataModel.HeatMap(months, emptyList()))
        )

        classUnderTest.write(long)

        assertEquals(3, pagesOf(written.single()))
    }

    @Test
    fun givenAVeryLongNote_whenWritten_thenItsRowStillFitsOnAPage() {
        val note = List(2_000) { "line $it" }.joinToString("\n")
        val tall = document(rows = 0).copy(
            blocks = listOf(
                ReportBlockDataModel.Table(
                    columns = listOf(ReportColumnDataModel("Note", 1f)),
                    rows = listOf(listOf(note)),
                    shortenedNote = null
                )
            )
        )

        classUnderTest.write(tall)

        assertEquals(1, pagesOf(written.single()))
    }

    @Test
    fun whenCleaningUp_thenOldReportsInTheirFoldersGoToo() {
        classUnderTest.write(document(rows = 1))
        val file = written.single()
        file.setLastModified(Instant.parse("2026-10-20T10:00:00Z").toEpochMilli())

        classUnderTest.cleanUp(Instant.parse("2026-10-25T10:00:00Z"))

        assertFalse(file.exists())
        assertFalse(file.parentFile!!.exists())
    }
}
