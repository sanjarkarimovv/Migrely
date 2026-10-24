package com.migrely.app.datasource.report.datasource

import com.migrely.app.datasource.file.file
import com.migrely.app.datasource.report.layout.ReportPageLayout
import com.migrely.app.datasource.report.model.ReportBlockDataModel
import com.migrely.app.datasource.report.model.ReportDocumentDataModel
import java.io.File
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.util.Locale
import java.util.UUID

private const val DIRECTORY = "reports"
private const val PARTIAL = ".partial"
private val KEPT_FOR: Duration = Duration.ofDays(1)

/**
 * Reports go to [cacheDirectory]`/reports`, which Android's backup leaves out (BRD D74, D85), and [uriOf] turns a
 * finished file into the content URI another app may read; in the app it is the FileProvider.
 */
class ReportDocumentDataSourceImpl(
    private val cacheDirectory: File,
    private val uriOf: (File) -> String
) : ReportDocumentDataSource {
    override fun write(document: ReportDocumentDataModel): String = file {
        // A folder of its own, so a second report the same day never takes over the first one's URI (review).
        val directory = File(File(cacheDirectory, DIRECTORY), UUID.randomUUID().toString()).apply {
            mkdirs()
        }
        val target = File(directory, document.fileName)
        // Written aside first, so an interrupted report never passes for a whole one (EC-REP-03).
        val partial = File(directory, document.fileName + PARTIAL)
        try {
            val layout = ReportPageLayout(document)
            val pdf = layout.render(fitted(document, layout), document.maxPages)
            try {
                partial.outputStream().use(pdf::writeTo)
            } finally {
                pdf.close()
            }
            target.delete()
            if (!partial.renameTo(
                    target
                )
            ) {
                throw IOException("Could not finish ${document.fileName}")
            }
        } finally {
            partial.delete()
        }
        uriOf(target)
    }

    override fun cleanUp(now: Instant) = file {
        File(cacheDirectory, DIRECTORY).walkBottomUp()
            .filter { it.isFile }
            .filter {
                it.name.endsWith(PARTIAL) ||
                    Instant.ofEpochMilli(it.lastModified()).isBefore(now.minus(KEPT_FOR))
            }
            .forEach(File::delete)
        // Report folders left empty go too.
        File(cacheDirectory, DIRECTORY).listFiles().orEmpty()
            .filter { it.isDirectory && it.list().isNullOrEmpty() }
            .forEach(File::delete)
    }

    /**
     * Within its page limit (EC-REP-02): while it would be longer, each table that may be shortened keeps, in turn, as
     * many of its first rows (the latest) as fit, and says how many it shows. Past that, `render` cuts the last pages.
     */
    private fun fitted(
        document: ReportDocumentDataModel,
        layout: ReportPageLayout
    ): List<ReportBlockDataModel> {
        var blocks = document.blocks
        document.blocks.filterIsInstance<ReportBlockDataModel.Table>()
            .filter { it.shortenedNote != null }
            .forEach { table ->
                if (layout.pageCount(blocks) > document.maxPages) {
                    blocks = shortened(blocks, table, document.maxPages, layout)
                }
            }
        return blocks
    }

    private fun shortened(
        blocks: List<ReportBlockDataModel>,
        table: ReportBlockDataModel.Table,
        maxPages: Int,
        layout: ReportPageLayout
    ): List<ReportBlockDataModel> {
        // Found by identity: a table shortened before this one added its note, so the positions moved.
        val offset = blocks.indexOfFirst { it === table }
        fun keeping(kept: Int): List<ReportBlockDataModel> = blocks.toMutableList().apply {
            set(offset, table.copy(rows = table.rows.take(kept)))
            add(
                offset + 1,
                ReportBlockDataModel.Paragraph(
                    String.format(Locale.ENGLISH, table.shortenedNote!!, kept, table.rows.size)
                )
            )
        }
        var low = 0
        var high = table.rows.size
        while (low < high) {
            val middle = (low + high + 1) / 2
            if (layout.pageCount(keeping(middle)) <= maxPages) low = middle else high = middle - 1
        }
        return keeping(low)
    }
}
