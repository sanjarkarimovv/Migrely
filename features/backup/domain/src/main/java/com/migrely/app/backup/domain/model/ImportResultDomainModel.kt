package com.migrely.app.backup.domain.model

sealed interface ImportResultDomainModel {
    data class Replaced(val summary: BackupSummaryDomainModel) : ImportResultDomainModel

    /**
     * [skippedAttacks] were already on the phone. [addedOthers] counts the medications, custom tags, intakes and marked
     * days added.
     */
    data class Added(val addedAttacks: Int, val skippedAttacks: Int, val addedOthers: Int) :
        ImportResultDomainModel

    data object NotABackup : ImportResultDomainModel

    data object Newer : ImportResultDomainModel

    /** Nothing changed: the phone is full, possibly while saving the automatic backup. */
    data object StorageFull : ImportResultDomainModel

    /** Nothing changed. */
    data object Failed : ImportResultDomainModel
}
