package com.migrely.app.history.ui.model

import com.migrely.app.designsystem.snackbar.SnackbarTone

/**
 * How a History message behaves (BRD EC-UX-16): its [tone], whether it offers "Undo" ([undoesDeletion]), and how long it
 * stays before the accessibility timeout is applied ([baseDurationMillis]); null stays until the user closes it.
 */
data class HistoryMessageBehaviourUiModel(
    val tone: SnackbarTone,
    val undoesDeletion: Boolean,
    val baseDurationMillis: Long?
)
