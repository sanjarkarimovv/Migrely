package com.migrely.app.report.presentation.mapper

import com.migrely.app.report.domain.model.ReportOptionsDomainModel
import com.migrely.app.report.presentation.model.ReportOptionsPresentationModel
import com.migrely.app.report.presentation.model.ReportViewState

class ReportOptionsPresentationMapper {
    fun toPresentation(options: ReportOptionsDomainModel): ReportViewState = when (options) {
        is ReportOptionsDomainModel.Ready -> ReportViewState.Ready(
            ReportOptionsPresentationModel(
                options.dates.from,
                options.dates.to,
                options.anythingLogged
            )
        )
        ReportOptionsDomainModel.Failed -> ReportViewState.Unavailable
    }
}
