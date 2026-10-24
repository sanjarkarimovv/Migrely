package com.migrely.app.backup.domain.model

sealed interface ImportResultDomainModel {
    data class Replaced(val summary: BackupSummaryDomainModel) : ImportResultDomainModel

    /** [skippedAttacks] were already on the phone. */
    data class Added(val addedAttacks: Int, val skippedAttacks: Int) : ImportResultDomainModel

    data object NotABackup : ImportResultDomainModel

    data object Newer : ImportResultDomainModel

    /** Nothing changed: the phone is full, possibly while saving the automatic backup. */
    data object StorageFull : ImportResultDomainModel

    /** Nothing changed. */
    data object Failed : ImportResultDomainModel
}
