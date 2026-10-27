package com.migrely.app.backup.ui.mapper

import com.migrely.app.backup.presentation.model.BackupSummaryPresentationModel
import com.migrely.app.backup.ui.model.BackupSummaryUiModel

class BackupSummaryUiMapper {
    fun toUi(summary: BackupSummaryPresentationModel) =
        BackupSummaryUiModel(summary.attacks, summary.medications, summary.dayMarks)
}
