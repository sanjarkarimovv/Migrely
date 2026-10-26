package com.migrely.app.datasource.report.datasource

import com.migrely.app.datasource.file.file
import com.migrely.app.datasource.report.layout.ReportPageLayout
import com.migrely.app.datasource.report.layout.fittedBlocks
import com.migrely.app.datasource.report.model.ReportDocumentDataModel
import java.io.File
import java.io.IOException
import java.time.Duration
import java.time.Instant
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
            val blocks = fittedBlocks(document.blocks, document.maxPages, layout::pageCount)
            val pdf = layout.render(blocks, document.maxPages)
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

    override fun deleteAll() = file {
        val directory = File(cacheDirectory, DIRECTORY)
        if (!directory.deleteRecursively()) throw IOException("Could not delete ${directory.name}")
    }
}
