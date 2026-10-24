package com.migrely.app.backup.presentation.mapper

import com.migrely.app.backup.domain.model.BackupSummaryDomainModel
import com.migrely.app.backup.presentation.model.BackupSummaryPresentationModel

class BackupSummaryPresentationMapper {
    fun toPresentation(summary: BackupSummaryDomainModel) = BackupSummaryPresentationModel(
        attacks = summary.attacks,
        medications = summary.medications,
        dayMarks = summary.dayMarks
    )
}
