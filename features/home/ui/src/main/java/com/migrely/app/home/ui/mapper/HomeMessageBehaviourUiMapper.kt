package com.migrely.app.home.ui.mapper

import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.home.ui.model.HomeMessageBehaviourUiModel
import com.migrely.app.home.ui.model.HomeMessageUiModel
import com.migrely.app.home.ui.model.HomeUndoTargetUiModel

private const val UNDO_MILLIS = 5_000L
private const val READING_MILLIS = 8_000L

/** Tone, undo and timing of each home message (BRD EC-UX-16, ATK-F1, ATK-F2). */
class HomeMessageBehaviourUiMapper {
    fun toUi(message: HomeMessageUiModel): HomeMessageBehaviourUiModel = when (message) {
        is HomeMessageUiModel.AttackStarted ->
            HomeMessageBehaviourUiModel(
                SnackbarTone.Confirmation,
                HomeUndoTargetUiModel.Start,
                UNDO_MILLIS
            )

        is HomeMessageUiModel.AttackEnded ->
            HomeMessageBehaviourUiModel(
                SnackbarTone.Confirmation,
                HomeUndoTargetUiModel.End,
                UNDO_MILLIS
            )

        HomeMessageUiModel.Undone,
        HomeMessageUiModel.UndoNotPossible,
        is HomeMessageUiModel.StartMoveRejected,
        HomeMessageUiModel.EndTimeOutOfRange ->
            HomeMessageBehaviourUiModel(SnackbarTone.Information, undoTarget = null, READING_MILLIS)

        HomeMessageUiModel.SomethingWentWrong ->
            HomeMessageBehaviourUiModel(SnackbarTone.Error, undoTarget = null, READING_MILLIS)

        // The last action was not saved, so the message stays until the user has seen it (EC-STO-01).
        HomeMessageUiModel.StorageFull ->
            HomeMessageBehaviourUiModel(
                SnackbarTone.Error,
                undoTarget = null,
                baseDurationMillis = null
            )
    }
}
