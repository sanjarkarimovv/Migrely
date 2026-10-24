package com.migrely.app.backup.domain.model

sealed interface BackupWriteResultDomainModel {
    data object Saved : BackupWriteResultDomainModel

    data object StorageFull : BackupWriteResultDomainModel

    /** The file could not be written, or the records could not be read. */
    data object Failed : BackupWriteResultDomainModel
}
