package com.migrely.app.history.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** A month of the calendar; [month] is 1 to 12 and [attacks] are newest first. */
@Parcelize
data class HistoryMonthUiModel(
    val year: Int,
    val month: Int,
    val todayEpochDay: Long,
    val hasPreviousMonth: Boolean,
    val hasNextMonth: Boolean,
    val days: List<HistoryDayUiModel>,
    val attacks: List<HistoryAttackUiModel>
) : Parcelable
