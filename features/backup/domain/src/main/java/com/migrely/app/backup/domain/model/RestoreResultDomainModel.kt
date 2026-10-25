package com.migrely.app.backup.domain.model

sealed interface RestoreResultDomainModel {
    /** The phone holds what the copy held. */
    data class Restored(val summary: BackupSummaryDomainModel) : RestoreResultDomainModel

    /** Nothing changed: the phone is full, possibly while saving what it holds first. */
    data object StorageFull : RestoreResultDomainModel

    /** Nothing changed: the copy is gone or cannot be read, or the phone could not be written. */
    data object Failed : RestoreResultDomainModel
}
