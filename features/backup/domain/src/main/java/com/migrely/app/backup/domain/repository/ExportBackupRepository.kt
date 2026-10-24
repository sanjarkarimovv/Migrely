package com.migrely.app.backup.domain.repository

import com.migrely.app.backup.domain.model.BackupWriteResultDomainModel

/** Writes every health record to the file at [uri] (BRD BAK-F1, D71). */
interface ExportBackupRepository {
    fun exportBackup(uri: String): BackupWriteResultDomainModel
}
