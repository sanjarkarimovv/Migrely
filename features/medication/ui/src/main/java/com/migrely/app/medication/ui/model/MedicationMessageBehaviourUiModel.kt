package com.migrely.app.medication.ui.model

import com.migrely.app.designsystem.snackbar.SnackbarTone

/**
 * How a medication message behaves (BRD EC-UX-16): its [tone], whether it offers "Undo" ([undoesArchive]), and its base
 * duration before the accessibility timeout is applied ([baseDurationMillis], null to stay until dismissed).
 */
data class MedicationMessageBehaviourUiModel(
    val tone: SnackbarTone,
    val undoesArchive: Boolean,
    val baseDurationMillis: Long?
)
