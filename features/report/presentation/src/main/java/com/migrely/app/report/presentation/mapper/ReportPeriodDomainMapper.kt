package com.migrely.app.report.presentation.mapper

import com.migrely.app.report.domain.model.ReportPeriodDomainModel
import com.migrely.app.report.presentation.model.ReportPeriodPresentationModel

class ReportPeriodDomainMapper {
    fun toDomain(period: ReportPeriodPresentationModel) = when (period) {
        is ReportPeriodPresentationModel.LastMonths -> ReportPeriodDomainModel.LastMonths(
            period.months
        )
        is ReportPeriodPresentationModel.Range -> ReportPeriodDomainModel.Range(
            period.from,
            period.to
        )
    }
}
