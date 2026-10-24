package com.migrely.app.report.domain.usecase

import com.migrely.app.report.domain.model.ReportDatesDomainModel
import com.migrely.app.report.domain.model.ReportPeriodDomainModel
import java.time.LocalDate

/** A custom range covers at most this many months, so a report always fits its pages (BRD D84, review I1). */
const val MAXIMUM_RANGE_MONTHS = 24L

/** The dates a period covers on [today] (BRD D81, D83): never past today, and a range at most 24 months long. */
internal fun ReportPeriodDomainModel.datesOn(today: LocalDate): ReportDatesDomainModel =
    when (this) {
        is ReportPeriodDomainModel.LastMonths -> ReportDatesDomainModel(
            today.minusMonths(months.toLong()).plusDays(1),
            today
        )
        is ReportPeriodDomainModel.Range -> {
            val to = minOf(to, today)
            ReportDatesDomainModel(
                maxOf(minOf(from, to), to.minusMonths(MAXIMUM_RANGE_MONTHS).plusDays(1)),
                to
            )
        }
    }
