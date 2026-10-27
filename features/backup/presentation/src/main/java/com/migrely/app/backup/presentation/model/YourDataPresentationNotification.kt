package com.migrely.app.backup.presentation.model

import com.migrely.app.architecture.presentation.notification.PresentationNotification

sealed interface YourDataPresentationNotification : PresentationNotification {
    data object BackupSaved : YourDataPresentationNotification

    data object CsvSaved : YourDataPresentationNotification

    /**
     * The picked file is a backup: show what it holds and ask how to import it, [uri] being passed back with the
     * answer. With nothing on the phone there is nothing to choose between (BRD EC-BAK-04).
     */
    data class ConfirmImport(
        val uri: String,
        val summary: BackupSummaryPresentationModel,
        val phoneHasRecords: Boolean
    ) : YourDataPresentationNotification

    data class Replaced(val summary: BackupSummaryPresentationModel) :
        YourDataPresentationNotification

    /** The automatic copy took the place of the phone's records (BRD D98). */
    data class Restored(val summary: BackupSummaryPresentationModel) :
        YourDataPresentationNotification

    /** [addedOthers] counts the medications, custom tags, intakes and marked days added. */
    data class Added(val addedAttacks: Int, val skippedAttacks: Int, val addedOthers: Int) :
        YourDataPresentationNotification

    data object NotABackup : YourDataPresentationNotification

    data object Newer : YourDataPresentationNotification

    data object StorageFull : YourDataPresentationNotification

    data object SomethingWentWrong : YourDataPresentationNotification
}
