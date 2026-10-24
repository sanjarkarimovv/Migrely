package com.migrely.app.history.presentation.model

import java.time.LocalDate
import java.time.YearMonth

/** A month of the calendar with its attacks, newest first (BRD HIS-F1, HIS-F2, EC-HIS-03). */
data class HistoryMonthPresentationModel(
    val month: YearMonth,
    val today: LocalDate,
    val hasPreviousMonth: Boolean,
    val hasNextMonth: Boolean,
    val days: List<HistoryDayPresentationModel>,
    val attacks: List<HistoryAttackPresentationModel>,
    val stats: MonthStatsPresentationModel
)
