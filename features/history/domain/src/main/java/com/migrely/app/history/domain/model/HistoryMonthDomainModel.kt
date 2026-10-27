package com.migrely.app.history.domain.model

import java.time.LocalDate
import java.time.YearMonth

/**
 * A month of History. [days] holds every day of [month] in order; [attacks] holds every attack that touched the month,
 * newest first (BRD HIS-F2). [earliestMonth] is the first month that can be shown (BRD EC-HIS-03).
 * [anythingLogged] is false until the first attack, day mark or attack-medication intake is
 * logged; preventive intakes do not count (BRD EC-HIS-01, D99).
 * [stats] are the month's numbers (BRD D53).
 */
data class HistoryMonthDomainModel(
    val month: YearMonth,
    val today: LocalDate,
    val earliestMonth: YearMonth,
    val anythingLogged: Boolean,
    val days: List<HistoryDayDomainModel>,
    val attacks: List<HistoryAttackDomainModel>,
    val stats: MonthStatsDomainModel,
    /** "Track periods" (BRD D79): while off, a day offers "Period" only to remove its mark. */
    val periodTracking: Boolean = true
)
