package com.migrely.app.report.presentation.mapper

import com.migrely.app.report.domain.model.ReportOptionsDomainModel
import com.migrely.app.report.domain.model.ReportPeriodDomainModel
import com.migrely.app.report.presentation.model.ReportOptionsPresentationModel
import com.migrely.app.report.presentation.model.ReportPeriodPresentationModel
import com.migrely.app.report.presentation.model.ReportViewState

class ReportOptionsPresentationMapper {
    fun toPresentation(options: ReportOptionsDomainModel): ReportViewState = when (options) {
        is ReportOptionsDomainModel.Ready -> ReportViewState.Ready(
            ReportOptionsPresentationModel(
                options.dates.from,
                options.dates.to,
                options.anythingLogged,
                toPresentation(options.period),
                options.lastVisit
            )
        )
        ReportOptionsDomainModel.Failed -> ReportViewState.Unavailable
    }

    private fun toPresentation(period: ReportPeriodDomainModel): ReportPeriodPresentationModel =
        when (period) {
            is ReportPeriodDomainModel.LastMonths ->
                ReportPeriodPresentationModel.LastMonths(period.months)
            is ReportPeriodDomainModel.Range ->
                ReportPeriodPresentationModel.Range(period.from, period.to)
            is ReportPeriodDomainModel.SinceLastVisit ->
                ReportPeriodPresentationModel.SinceLastVisit(period.from)
            ReportPeriodDomainModel.Default -> ReportPeriodPresentationModel.Default
        }
}
