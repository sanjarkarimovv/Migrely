package com.migrely.app.backup.ui.model

/** What the screen does in reply to the view model: ask to confirm an import, or show a message. */
sealed interface YourDataEventUiModel {
    data class Confirm(val pending: PendingImportUiModel) : YourDataEventUiModel

    data class Message(val message: YourDataMessageUiModel) : YourDataEventUiModel
}

sealed interface YourDataMessageUiModel {
    data object BackupSaved : YourDataMessageUiModel

    data object CsvSaved : YourDataMessageUiModel

    data class Replaced(val attacks: Int) : YourDataMessageUiModel

    data class Restored(val attacks: Int) : YourDataMessageUiModel

    data class Added(val addedAttacks: Int, val skippedAttacks: Int, val addedOthers: Int) :
        YourDataMessageUiModel

    data object NotABackup : YourDataMessageUiModel

    data object Newer : YourDataMessageUiModel

    data object StorageFull : YourDataMessageUiModel

    data object SomethingWentWrong : YourDataMessageUiModel

    /** The phone has no app to pick or create a file with. */
    data object NoFilePicker : YourDataMessageUiModel
}
