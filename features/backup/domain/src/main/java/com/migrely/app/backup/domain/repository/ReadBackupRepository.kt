package com.migrely.app.backup.domain.repository

import com.migrely.app.backup.domain.model.BackupFileDomainModel

interface ReadBackupRepository {
    fun readBackup(uri: String): BackupFileDomainModel
}
