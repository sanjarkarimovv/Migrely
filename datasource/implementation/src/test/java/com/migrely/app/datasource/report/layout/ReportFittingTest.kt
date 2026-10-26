package com.migrely.app.datasource.report.layout

import com.migrely.app.datasource.report.model.ReportBlockDataModel
import com.migrely.app.datasource.report.model.ReportColumnDataModel
import com.migrely.app.datasource.report.model.ReportFollowedTableDataModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

private const val LINES_PER_PAGE = 10
private const val MIGRAINES_SHORTENED =
    "Showing the latest %1\$d of %2\$d migraines. The summary counts all of them."
private const val NOTES_SHORTENED = "Notes from the latest %1\$d migraines."

/** A page holds ten lines; a table takes a line for its header and one per row, and any other block one line. */
private fun pageCount(blocks: List<ReportBlockDataModel>): Int {
    val lines = blocks.sumOf { if (it is ReportBlockDataModel.Table) 1 + it.rows.size else 1 }
    return (lines + LINES_PER_PAGE - 1) / LINES_PER_PAGE
}

private fun migraines(count: Int) = ReportBlockDataModel.Table(
    columns = listOf(ReportColumnDataModel("Start", 1f)),
    rows = List(count) { listOf("Migraine $it") },
    shortenedNote = MIGRAINES_SHORTENED
)

/** The notes of the migraines at [owners], each written as "Note <owner>". */
private fun notes(migraines: ReportBlockDataModel.Table, owners: List<Int>) =
    ReportBlockDataModel.Table(
        columns = listOf(ReportColumnDataModel("Note", 1f)),
        rows = owners.map { listOf("Note $it") },
        follows = ReportFollowedTableDataModel(migraines, owners, NOTES_SHORTENED)
    )

private fun report(
    migraines: ReportBlockDataModel.Table,
    notes: ReportBlockDataModel.Table?
): List<ReportBlockDataModel> = listOfNotNull(
    ReportBlockDataModel.Heading("Migraines"),
    migraines,
    ReportBlockDataModel.Heading("Medications"),
    ReportBlockDataModel.Paragraph("No medication was logged in this period."),
    notes?.let { ReportBlockDataModel.Heading("Notes") },
    notes
)

class ReportFittingTest {
    @Test
    fun `Given 900 migraines with notes then the notes of the migraines shown, after a line`() {
        val migraines = migraines(900)
        val blocks = report(migraines, notes(migraines, List(900) { it }))

        val fitted = fittedBlocks(blocks, maxPages = 50, pageCount = ::pageCount)

        // 500 lines: 3 headings, the medications line, 2 lines saying what is shown, 2 headers, 2 × 246 rows.
        assertEquals(
            listOf(
                ReportBlockDataModel.Heading("Migraines"),
                ReportBlockDataModel.Paragraph(
                    "Showing the latest 246 of 900 migraines. The summary counts all of them."
                ),
                migraines.copy(rows = migraines.rows.take(246)),
                ReportBlockDataModel.Heading("Medications"),
                ReportBlockDataModel.Paragraph("No medication was logged in this period."),
                ReportBlockDataModel.Heading("Notes"),
                ReportBlockDataModel.Paragraph("Notes from the latest 246 migraines.")
            ),
            fitted.dropLast(1)
        )
        assertEquals(
            List(246) { listOf("Note $it") },
            (fitted.last() as ReportBlockDataModel.Table).rows
        )
        assertEquals(50, pageCount(fitted))
    }

    @Test
    fun `Given notes on some migraines only then only those of the migraines shown`() {
        val migraines = migraines(900)
        val blocks = report(migraines, notes(migraines, (0 until 900 step 3).toList()))

        val fitted = fittedBlocks(blocks, maxPages = 50, pageCount = ::pageCount)

        val shown = (fitted[2] as ReportBlockDataModel.Table).rows.size
        val notes = fitted.last() as ReportBlockDataModel.Table
        assertEquals((0 until shown step 3).map { listOf("Note $it") }, notes.rows)
        assertEquals(
            ReportBlockDataModel.Paragraph("Notes from the latest $shown migraines."),
            fitted[fitted.size - 2]
        )
        assertEquals(50, pageCount(fitted))
    }

    @Test
    fun `Given notes on the oldest migraines only then the line and an empty notes table`() {
        val migraines = migraines(900)
        val blocks = report(migraines, notes(migraines, (800 until 900).toList()))

        val fitted = fittedBlocks(blocks, maxPages = 50, pageCount = ::pageCount)

        val shown = (fitted[2] as ReportBlockDataModel.Table).rows.size
        assertEquals(
            ReportBlockDataModel.Paragraph("Notes from the latest $shown migraines."),
            fitted[fitted.size - 2]
        )
        assertEquals(emptyList<List<String>>(), (fitted.last() as ReportBlockDataModel.Table).rows)
    }

    @Test
    fun `Given few migraines with notes then every note and no line`() {
        val migraines = migraines(20)
        val blocks = report(migraines, notes(migraines, List(20) { it }))

        assertSame(blocks, fittedBlocks(blocks, maxPages = 50, pageCount = ::pageCount))
    }

    @Test
    fun `Given no notes then the migraines table alone is shortened, as before`() {
        val migraines = migraines(900)
        val blocks = report(migraines, notes = null)

        val fitted = fittedBlocks(blocks, maxPages = 50, pageCount = ::pageCount)

        assertEquals(
            listOf(
                ReportBlockDataModel.Heading("Migraines"),
                ReportBlockDataModel.Paragraph(
                    "Showing the latest 495 of 900 migraines. The summary counts all of them."
                ),
                migraines.copy(rows = migraines.rows.take(495)),
                ReportBlockDataModel.Heading("Medications"),
                ReportBlockDataModel.Paragraph("No medication was logged in this period.")
            ),
            fitted
        )
    }
}
