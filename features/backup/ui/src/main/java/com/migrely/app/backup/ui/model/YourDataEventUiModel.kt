package com.migrely.app.backup.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** What the screen does in reply to the view model: ask to confirm an import, or show a message. */
sealed interface YourDataEventUiModel {
    data class Confirm(val pending: PendingImportUiModel) : YourDataEventUiModel

    data class Message(val message: YourDataMessageUiModel) : YourDataEventUiModel
}

/** A message on Your data. Parcelable, so an error on screen survives rotation and a restored process (BRD D178 (2)). */
sealed interface YourDataMessageUiModel : Parcelable {
    @Parcelize
    data object BackupSaved : YourDataMessageUiModel

    @Parcelize
    data object CsvSaved : YourDataMessageUiModel

    @Parcelize
    data class Replaced(val attacks: Int) : YourDataMessageUiModel

    @Parcelize
    data class Restored(val attacks: Int) : YourDataMessageUiModel

    @Parcelize
    data class Added(val addedAttacks: Int, val skippedAttacks: Int, val addedOthers: Int) :
        YourDataMessageUiModel

    @Parcelize
    data object NotABackup : YourDataMessageUiModel

    @Parcelize
    data object Newer : YourDataMessageUiModel

    @Parcelize
    data object StorageFull : YourDataMessageUiModel

    @Parcelize
    data object SomethingWentWrong : YourDataMessageUiModel

    /** The phone has no app to pick or create a file with. */
    @Parcelize
    data object NoFilePicker : YourDataMessageUiModel
}
