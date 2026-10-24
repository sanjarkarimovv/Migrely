package com.migrely.app.attack.ui.model

import com.migrely.app.designsystem.snackbar.SnackbarTone

/** Tone, undo and base duration of a details message (BRD EC-UX-16); a null duration stays until dismissed. */
data class AttackMessageBehaviourUiModel(
    val tone: SnackbarTone,
    val hasUndo: Boolean,
    val baseDurationMillis: Long?
)
