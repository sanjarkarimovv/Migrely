package com.migrely.app.history.ui.mapper

import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.history.ui.model.HistoryMessageBehaviourUiModel
import com.migrely.app.history.ui.model.HistoryMessageUiModel

private const val UNDO_MILLIS = 5_000L
private const val READING_MILLIS = 8_000L

/** Tone, undo and timing of each History message, as on home (BRD EC-UX-16, D31). */
class HistoryMessageBehaviourUiMapper {
    fun toUi(message: HistoryMessageUiModel): HistoryMessageBehaviourUiModel = when (message) {
        is HistoryMessageUiModel.AttackDeleted ->
            HistoryMessageBehaviourUiModel(SnackbarTone.Confirmation, true, UNDO_MILLIS)

        HistoryMessageUiModel.Undone,
        HistoryMessageUiModel.UndoNotPossible,
        HistoryMessageUiModel.AttackLoggedOnDay ->
            HistoryMessageBehaviourUiModel(SnackbarTone.Information, false, READING_MILLIS)

        // Stays until the user closes it (BRD EC-UX-16).
        HistoryMessageUiModel.StorageFull ->
            HistoryMessageBehaviourUiModel(SnackbarTone.Error, false, null)

        HistoryMessageUiModel.SomethingWentWrong ->
            HistoryMessageBehaviourUiModel(SnackbarTone.Error, false, READING_MILLIS)
    }
}
