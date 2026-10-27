package com.migrely.app.backup.domain.repository

import com.migrely.app.backup.domain.model.BackupWriteResultDomainModel

/** Writes one row per attack to the file at [uri] (BRD BAK-F2, D72). */
interface ExportCsvRepository {
    fun exportCsv(uri: String): BackupWriteResultDomainModel
}
