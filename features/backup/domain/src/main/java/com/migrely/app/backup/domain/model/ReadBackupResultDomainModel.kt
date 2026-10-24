package com.migrely.app.backup.domain.model

sealed interface ReadBackupResultDomainModel {
    /** [phoneHasRecords] decides whether the user is asked to replace or add (BRD EC-BAK-04). */
    data class Readable(val summary: BackupSummaryDomainModel, val phoneHasRecords: Boolean) :
        ReadBackupResultDomainModel

    data object NotABackup : ReadBackupResultDomainModel

    data object Newer : ReadBackupResultDomainModel

    data object Failed : ReadBackupResultDomainModel
}
