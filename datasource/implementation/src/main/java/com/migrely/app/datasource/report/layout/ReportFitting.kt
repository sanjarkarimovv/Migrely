package com.migrely.app.datasource.report.layout

import com.migrely.app.datasource.report.model.ReportBlockDataModel
import java.util.Locale

/**
 * Within its page limit (EC-REP-02): while [blocks] would take more than [maxPages], as [pageCount] counts them, each
 * table that may be shortened keeps, in turn, as many of its first rows (the latest) as fit, and says how many it
 * shows. A table that follows it, such as the notes of the migraines listed, keeps only its rows whose owner is among
 * the rows kept, and says so; with none kept, only its header is left, as for a table kept to no rows (D84). Past
 * that, the renderer cuts the last pages.
 */
internal fun fittedBlocks(
    blocks: List<ReportBlockDataModel>,
    maxPages: Int,
    pageCount: (List<ReportBlockDataModel>) -> Int
): List<ReportBlockDataModel> {
    var fitted = blocks
    blocks.filterIsInstance<ReportBlockDataModel.Table>()
        .filter { it.shortenedNote != null }
        .forEach { table ->
            if (pageCount(fitted) > maxPages) {
                fitted = shortened(fitted, table, maxPages, pageCount)
            }
        }
    return fitted
}

private fun shortened(
    blocks: List<ReportBlockDataModel>,
    table: ReportBlockDataModel.Table,
    maxPages: Int,
    pageCount: (List<ReportBlockDataModel>) -> Int
): List<ReportBlockDataModel> {
    // Found by identity: a table shortened before this one added its note, so the positions moved.
    val offset = blocks.indexOfFirst { it === table }
    val followers = blocks.indices.filter {
        (blocks[it] as? ReportBlockDataModel.Table)?.follows?.table === table
    }

    fun keeping(kept: Int): List<ReportBlockDataModel> = blocks.toMutableList().apply {
        // The last first, so the note added before a table leaves the positions before it in place.
        (followers + offset).sortedDescending().forEach { index ->
            val shown = blocks[index] as ReportBlockDataModel.Table
            val (rows, note) = if (index == offset) {
                shown.rows.take(kept) to
                    String.format(Locale.ENGLISH, shown.shortenedNote!!, kept, shown.rows.size)
            } else {
                val followed = shown.follows!!
                shown.rows.filterIndexed { row, _ -> followed.owners[row] < kept } to
                    String.format(Locale.ENGLISH, followed.shortenedNote, kept)
            }
            set(index, shown.copy(rows = rows))
            // Before the table, where a reader starts it (walk B3).
            add(index, ReportBlockDataModel.Paragraph(note))
        }
    }

    var low = 0
    var high = table.rows.size
    while (low < high) {
        val middle = (low + high + 1) / 2
        if (pageCount(keeping(middle)) <= maxPages) low = middle else high = middle - 1
    }
    return keeping(low)
}
