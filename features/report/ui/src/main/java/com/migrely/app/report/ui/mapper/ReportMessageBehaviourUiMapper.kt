package com.migrely.app.report.ui.mapper

import com.migrely.app.designsystem.snackbar.SnackbarTone
import com.migrely.app.report.ui.model.ReportMessageBehaviourUiModel
import com.migrely.app.report.ui.model.ReportMessageUiModel

private const val READING_MILLIS = 8_000L

/** Tone and timing of each report message (BRD EC-UX-16, EC-STO-01; QA #56). */
class ReportMessageBehaviourUiMapper {
    fun toUi(message: ReportMessageUiModel): ReportMessageBehaviourUiModel = when (message) {
        ReportMessageUiModel.NothingLogged,
        ReportMessageUiModel.SomethingWentWrong,
        ReportMessageUiModel.NoAppToShare ->
            ReportMessageBehaviourUiModel(SnackbarTone.Error, READING_MILLIS)

        // No report was made, so the message stays until the user has seen it (EC-STO-01).
        ReportMessageUiModel.StorageFull ->
            ReportMessageBehaviourUiModel(SnackbarTone.Error, baseDurationMillis = null)
    }
}
