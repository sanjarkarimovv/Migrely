package com.migrely.app.report.ui.model

import com.migrely.app.designsystem.snackbar.SnackbarTone

/**
 * How a report message behaves (BRD EC-UX-16): its [tone], and how long it stays before the accessibility timeout is
 * applied ([baseDurationMillis]); null stays until the user closes it.
 */
data class ReportMessageBehaviourUiModel(val tone: SnackbarTone, val baseDurationMillis: Long?)
