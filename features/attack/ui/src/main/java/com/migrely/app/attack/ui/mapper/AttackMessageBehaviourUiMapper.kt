package com.migrely.app.attack.ui.mapper

import com.migrely.app.attack.ui.model.AttackMessageBehaviourUiModel
import com.migrely.app.attack.ui.model.AttackMessageUiModel
import com.migrely.app.designsystem.snackbar.SnackbarTone

private const val UNDO_MILLIS = 5_000L
private const val READING_MILLIS = 8_000L

/** Tone, undo and timing of each details message (BRD EC-UX-16, ATK-F7, EC-ATK-09). */
class AttackMessageBehaviourUiMapper {
    fun toUi(message: AttackMessageUiModel): AttackMessageBehaviourUiModel = when (message) {
        is AttackMessageUiModel.PointsRemovedByTimeChange,
        is AttackMessageUiModel.PointRemoved ->
            AttackMessageBehaviourUiModel(
                SnackbarTone.Information,
                hasUndo = true,
                baseDurationMillis = UNDO_MILLIS
            )

        AttackMessageUiModel.PointOutsideAttack,
        AttackMessageUiModel.TagLimitReached,
        AttackMessageUiModel.Undone,
        AttackMessageUiModel.UndoNotPossible ->
            AttackMessageBehaviourUiModel(
                SnackbarTone.Information,
                hasUndo = false,
                baseDurationMillis = READING_MILLIS
            )

        AttackMessageUiModel.SomethingWentWrong ->
            AttackMessageBehaviourUiModel(
                SnackbarTone.Error,
                hasUndo = false,
                baseDurationMillis = READING_MILLIS
            )

        // What the user entered was not saved, so the message stays until it has been seen (EC-STO-02).
        AttackMessageUiModel.StorageFull ->
            AttackMessageBehaviourUiModel(
                SnackbarTone.Error,
                hasUndo = false,
                baseDurationMillis = null
            )
    }
}
