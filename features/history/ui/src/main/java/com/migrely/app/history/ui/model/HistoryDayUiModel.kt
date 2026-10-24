package com.migrely.app.history.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** One calendar day. [peak] is null when nothing touched the day; [attacks] are oldest first. */
@Parcelize
data class HistoryDayUiModel(
    val epochDay: Long,
    val sky: DaySkyUiModel,
    val peak: PeakUiModel?,
    val attacks: List<HistoryAttackUiModel>
) : Parcelable
