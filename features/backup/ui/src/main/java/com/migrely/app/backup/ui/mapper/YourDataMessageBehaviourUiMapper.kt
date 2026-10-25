package com.migrely.app.backup.ui.mapper

import com.migrely.app.backup.ui.model.YourDataMessageBehaviourUiModel
import com.migrely.app.backup.ui.model.YourDataMessageUiModel
import com.migrely.app.designsystem.snackbar.SnackbarTone

private const val READING_MILLIS = 8_000L

/** Tone and timing of each Your data message (BRD EC-UX-16, EC-STO-01; QA #56). */
class YourDataMessageBehaviourUiMapper {
    fun toUi(message: YourDataMessageUiModel): YourDataMessageBehaviourUiModel = when (message) {
        YourDataMessageUiModel.BackupSaved,
        YourDataMessageUiModel.CsvSaved,
        is YourDataMessageUiModel.Replaced,
        is YourDataMessageUiModel.Restored ->
            YourDataMessageBehaviourUiModel(SnackbarTone.Confirmation, READING_MILLIS)

        is YourDataMessageUiModel.Added ->
            YourDataMessageBehaviourUiModel(
                if (message.addedAttacks == 0 && message.addedOthers == 0) {
                    SnackbarTone.Information
                } else {
                    SnackbarTone.Confirmation
                },
                READING_MILLIS
            )

        YourDataMessageUiModel.NotABackup,
        YourDataMessageUiModel.Newer,
        YourDataMessageUiModel.SomethingWentWrong,
        YourDataMessageUiModel.NoFilePicker ->
            YourDataMessageBehaviourUiModel(SnackbarTone.Error, READING_MILLIS)

        // Nothing was saved, so the message stays until the user has seen it (EC-STO-01).
        YourDataMessageUiModel.StorageFull ->
            YourDataMessageBehaviourUiModel(SnackbarTone.Error, baseDurationMillis = null)
    }
}
