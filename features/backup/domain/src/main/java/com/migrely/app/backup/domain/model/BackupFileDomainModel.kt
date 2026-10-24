package com.migrely.app.backup.domain.model

/** What a picked file turned out to be (BRD EC-BAK-01–03). */
sealed interface BackupFileDomainModel {
    data class Readable(val summary: BackupSummaryDomainModel) : BackupFileDomainModel

    data object NotABackup : BackupFileDomainModel

    data object Newer : BackupFileDomainModel

    /** The file could not be read at all: gone, unreadable or too large. */
    data object Failed : BackupFileDomainModel
}
