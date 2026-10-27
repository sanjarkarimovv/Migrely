package com.migrely.app.reminders.ui.model

import com.migrely.app.designsystem.snackbar.SnackbarTone

/**
 * How a Reminders message behaves (BRD EC-UX-16): its [tone], and how long it stays before the accessibility timeout is
 * applied ([baseDurationMillis]); null stays until the user closes it.
 */
data class RemindersMessageBehaviourUiModel(val tone: SnackbarTone, val baseDurationMillis: Long?)
