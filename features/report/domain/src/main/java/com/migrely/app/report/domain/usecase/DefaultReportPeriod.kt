package com.migrely.app.report.domain.usecase

import com.migrely.app.report.domain.model.ReportPeriodDomainModel
import java.time.LocalDate

private const val DEFAULT_MONTHS = 3

/**
 * The period a report covers when nobody chose one (BRD EC-REP-04, design D4): since the last visit when it was within
 * 24 months, otherwise the last 3 months. The reminders feature has its own copy, since features do not depend on each
 * other; both tests run the same table of cases.
 */
internal object DefaultReportPeriod {
    /** The latest visit before [today], when it is within 24 months; a visit on [today] is not the last one yet. */
    fun lastVisit(visits: Set<LocalDate>, today: LocalDate): LocalDate? =
        visits.filter { it.isBefore(today) }.maxOrNull()
            ?.takeIf { !it.isBefore(today.minusMonths(MAXIMUM_RANGE_MONTHS)) }

    fun of(lastVisit: LocalDate?, today: LocalDate): ReportPeriodDomainModel =
        lastVisit?.takeIf { !it.isBefore(today.minusMonths(MAXIMUM_RANGE_MONTHS)) }
            ?.let(ReportPeriodDomainModel::SinceLastVisit)
            ?: ReportPeriodDomainModel.LastMonths(DEFAULT_MONTHS)

    fun resolve(
        period: ReportPeriodDomainModel,
        lastVisit: LocalDate?,
        today: LocalDate
    ): ReportPeriodDomainModel = if (period ==
        ReportPeriodDomainModel.Default
    ) {
        of(lastVisit, today)
    } else {
        period
    }
}
