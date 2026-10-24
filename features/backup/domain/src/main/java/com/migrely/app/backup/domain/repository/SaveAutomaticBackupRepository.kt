package com.migrely.app.backup.domain.repository

import com.migrely.app.backup.domain.model.BackupWriteResultDomainModel

/** Saves what the phone holds now inside the app, keeping the last three (BRD D73). */
interface SaveAutomaticBackupRepository {
    fun saveAutomaticBackup(): BackupWriteResultDomainModel
}
