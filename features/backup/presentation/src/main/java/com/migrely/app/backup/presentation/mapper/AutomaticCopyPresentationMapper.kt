package com.migrely.app.backup.presentation.mapper

import com.migrely.app.backup.domain.model.AutomaticCopyDomainModel
import com.migrely.app.backup.presentation.model.AutomaticCopyPresentationModel

class AutomaticCopyPresentationMapper(
    private val backupSummaryPresentationMapper: BackupSummaryPresentationMapper
) {
    /** Null when there is no copy to offer. */
    fun toPresentation(copy: AutomaticCopyDomainModel): AutomaticCopyPresentationModel? =
        when (copy) {
            is AutomaticCopyDomainModel.Available -> AutomaticCopyPresentationModel(
                madeAt = copy.madeAt,
                summary = backupSummaryPresentationMapper.toPresentation(copy.summary)
            )

            AutomaticCopyDomainModel.None -> null
        }
}
