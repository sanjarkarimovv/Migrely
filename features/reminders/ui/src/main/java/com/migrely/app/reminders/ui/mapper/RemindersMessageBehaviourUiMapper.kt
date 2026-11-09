package com.migrely.app.reminders.ui.mapper

import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.reminders.ui.model.RemindersMessageBehaviourUiModel
import com.migrely.app.reminders.ui.model.RemindersMessageUiModel

private const val READING_MILLIS = 8_000L
private const val UNDO_MILLIS = 5_000L

/** Tone and timing of each Reminders message (BRD EC-UX-16, EC-STO-01; QA #56). */
class RemindersMessageBehaviourUiMapper {
    fun toUi(message: RemindersMessageUiModel): RemindersMessageBehaviourUiModel = when (message) {
        RemindersMessageUiModel.SomethingWentWrong ->
            RemindersMessageBehaviourUiModel(SnackbarTone.Error, READING_MILLIS)

        // The setting was not saved, so the message stays until the user has seen it (EC-STO-01).
        // Undo for 5 seconds, or the longer accessibility timeout (BRD EC-REM-10, D158).
        is RemindersMessageUiModel.VisitRemoved ->
            RemindersMessageBehaviourUiModel(SnackbarTone.Information, UNDO_MILLIS)

        RemindersMessageUiModel.Undone ->
            RemindersMessageBehaviourUiModel(SnackbarTone.Information, READING_MILLIS)

        RemindersMessageUiModel.StorageFull ->
            RemindersMessageBehaviourUiModel(SnackbarTone.Error, baseDurationMillis = null)
    }
}
