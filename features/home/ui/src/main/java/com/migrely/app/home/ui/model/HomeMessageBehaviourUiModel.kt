package com.migrely.app.home.ui.model

import com.migrely.app.designsystem.snackbar.SnackbarTone

/**
 * How a home message behaves (BRD EC-UX-16): its [tone], what its "Undo" undoes ([undoTarget], null when it has no
 * undo), and how long it stays before the accessibility timeout is applied ([baseDurationMillis], null when it stays
 * until the user dismisses it).
 */
data class HomeMessageBehaviourUiModel(
    val tone: SnackbarTone,
    val undoTarget: HomeUndoTargetUiModel?,
    val baseDurationMillis: Long?
)

enum class HomeUndoTargetUiModel { Start, End, Delete }
