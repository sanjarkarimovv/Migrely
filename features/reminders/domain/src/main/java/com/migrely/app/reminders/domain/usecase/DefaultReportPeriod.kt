package com.migrely.app.reminders.domain.usecase

import java.time.LocalDate

private const val SINCE_VISIT_MONTHS = 24L
private const val DEFAULT_MONTHS = 3L

/**
 * The period a doctor report covers when nobody chose one (BRD EC-REP-04, design D4): since the last visit when it was
 * within 24 months, otherwise the last 3 months. The report feature has its own copy, since features do not depend on
 * each other; both tests run the same table of cases.
 */
internal object DefaultReportPeriod {
    fun firstDay(lastVisit: LocalDate?, today: LocalDate): LocalDate =
        lastVisit?.takeIf { !it.isBefore(today.minusMonths(SINCE_VISIT_MONTHS)) }
            ?: today.minusMonths(DEFAULT_MONTHS).plusDays(1)
}
