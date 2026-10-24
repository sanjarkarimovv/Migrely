package com.migrely.app.history.domain.model

import java.time.LocalDate
import java.time.YearMonth

/**
 * A month of History. [days] holds every day of [month] in order; [attacks] holds every attack that touched the month,
 * newest first (BRD HIS-F2). [earliestMonth] is the first month that can be shown (BRD EC-HIS-03).
 */
data class HistoryMonthDomainModel(
    val month: YearMonth,
    val today: LocalDate,
    val earliestMonth: YearMonth,
    val days: List<HistoryDayDomainModel>,
    val attacks: List<HistoryAttackDomainModel>
)
