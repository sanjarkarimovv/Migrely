package com.migrely.app.datasource.report.datasource

import com.migrely.app.datasource.report.model.ReportDocumentDataModel
import java.time.Instant

/**
 * Doctor reports as PDF files in the app's cache, which Android's backup leaves out (BRD REP-F4, EC-REP-03, D85). Every
 * function blocks. A full phone throws [com.migrely.app.datasource.exception.StorageFullDataException]; any other
 * failure to write throws [com.migrely.app.datasource.exception.FileDataException].
 */
interface ReportDocumentDataSource {
    /** Renders [document] to a new file and returns a content URI another app can be given to read it. */
    fun write(document: ReportDocumentDataModel): String

    /** Removes files left half-written, and reports made more than a day before [now]. */
    fun cleanUp(now: Instant)

    /** Deletes every report, finished or not (BRD D96). */
    fun deleteAll()
}
