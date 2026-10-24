package com.migrely.app.datasource.report.datasource

import com.migrely.app.datasource.file.file
import com.migrely.app.datasource.report.layout.ReportPageLayout
import com.migrely.app.datasource.report.model.ReportBlockDataModel
import com.migrely.app.datasource.report.model.ReportDocumentDataModel
import java.io.File
import java.io.IOException
import java.time.Duration
import java.time.Instant

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
        val directory = File(cacheDirectory, DIRECTORY).apply { mkdirs() }
        val target = File(directory, document.fileName)
        // Written aside first, so an interrupted report never passes for a whole one (EC-REP-03).
        val partial = File(directory, document.fileName + PARTIAL)
        try {
            val layout = ReportPageLayout(document)
            val pdf = layout.render(fitted(document, layout))
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
        File(cacheDirectory, DIRECTORY).listFiles().orEmpty()
            .filter {
                it.name.endsWith(PARTIAL) ||
                    Instant.ofEpochMilli(it.lastModified()).isBefore(now.minus(KEPT_FOR))
            }
            .forEach(File::delete)
    }

    /**
     * Within its page limit (EC-REP-02): when it would be longer, the table that may be shortened keeps as many of its
     * first rows (the latest) as fit, and says how many it shows.
     */
    private fun fitted(
        document: ReportDocumentDataModel,
        layout: ReportPageLayout
    ): List<ReportBlockDataModel> {
        if (layout.pageCount(document.blocks) <= document.maxPages) return document.blocks
        val index = document.blocks.indexOfFirst {
            it is ReportBlockDataModel.Table &&
                it.shortenedNote != null
        }
        if (index < 0) return document.blocks
        val table = document.blocks[index] as ReportBlockDataModel.Table
        fun shortened(kept: Int): List<ReportBlockDataModel> =
            document.blocks.toMutableList().apply {
                set(index, table.copy(rows = table.rows.take(kept)))
                add(
                    index + 1,
                    ReportBlockDataModel.Paragraph(
                        table.shortenedNote!!.format(kept, table.rows.size)
                    )
                )
            }
        var low = 0
        var high = table.rows.size
        while (low < high) {
            val middle = (low + high + 1) / 2
            if (layout.pageCount(shortened(middle)) <=
                document.maxPages
            ) {
                low = middle
            } else {
                high = middle - 1
            }
        }
        return shortened(low)
    }
}
