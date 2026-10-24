package com.migrely.app.report.domain.usecase

import com.migrely.app.report.domain.model.ReportDatesDomainModel
import com.migrely.app.report.domain.model.ReportPeriodDomainModel
import java.time.LocalDate

/** The dates a period covers on [today] (BRD D81, D83): never past today. */
internal fun ReportPeriodDomainModel.datesOn(today: LocalDate): ReportDatesDomainModel =
    when (this) {
        is ReportPeriodDomainModel.LastMonths -> ReportDatesDomainModel(
            today.minusMonths(months.toLong()).plusDays(1),
            today
        )
        is ReportPeriodDomainModel.Range -> {
            val to = minOf(to, today)
            ReportDatesDomainModel(minOf(from, to), to)
        }
    }
