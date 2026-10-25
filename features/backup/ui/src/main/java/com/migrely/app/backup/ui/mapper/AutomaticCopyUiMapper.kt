package com.migrely.app.backup.ui.mapper

import com.migrely.app.backup.presentation.model.AutomaticCopyPresentationModel
import com.migrely.app.backup.ui.model.AutomaticCopyUiModel

class AutomaticCopyUiMapper(private val backupSummaryUiMapper: BackupSummaryUiMapper) {
    fun toUi(copy: AutomaticCopyPresentationModel) = AutomaticCopyUiModel(
        madeAtEpochMillis = copy.madeAt.toEpochMilli(),
        summary = backupSummaryUiMapper.toUi(copy.summary)
    )
}
