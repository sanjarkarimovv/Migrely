package com.migrely.app.medication.ui.mapper

import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.medication.ui.model.MedicationMessageBehaviourUiModel
import com.migrely.app.medication.ui.model.MedicationMessageUiModel

private const val UNDO_MILLIS = 5_000L
private const val READING_MILLIS = 8_000L

/** Tone, undo and timing of each medication message (BRD EC-UX-16). */
class MedicationMessageBehaviourUiMapper {
    fun toUi(message: MedicationMessageUiModel): MedicationMessageBehaviourUiModel =
        when (message) {
            is MedicationMessageUiModel.Archived ->
                MedicationMessageBehaviourUiModel(SnackbarTone.Confirmation, true, UNDO_MILLIS)

            MedicationMessageUiModel.Undone,
            MedicationMessageUiModel.IntakeUndone,
            MedicationMessageUiModel.InFuture,
            MedicationMessageUiModel.LimitReached ->
                MedicationMessageBehaviourUiModel(SnackbarTone.Information, false, READING_MILLIS)

            MedicationMessageUiModel.SomethingWentWrong ->
                MedicationMessageBehaviourUiModel(SnackbarTone.Error, false, READING_MILLIS)

            // Nothing was saved, so the message stays until the user has seen it (EC-STO-01).
            MedicationMessageUiModel.StorageFull ->
                MedicationMessageBehaviourUiModel(SnackbarTone.Error, false, null)
        }
}
