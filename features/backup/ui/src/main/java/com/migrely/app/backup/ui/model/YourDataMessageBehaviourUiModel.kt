package com.migrely.app.backup.ui.model

import com.migrely.app.designsystem.snackbar.SnackbarTone

/**
 * How a Your data message behaves (BRD EC-UX-16): its [tone], and how long it stays before the accessibility timeout is
 * applied ([baseDurationMillis]); null stays until the user closes it.
 */
data class YourDataMessageBehaviourUiModel(val tone: SnackbarTone, val baseDurationMillis: Long?)
