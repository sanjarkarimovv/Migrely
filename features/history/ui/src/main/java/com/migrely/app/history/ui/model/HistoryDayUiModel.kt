package com.migrely.app.history.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * One calendar day. [peak] is null when nothing touched the day; [attacks] are oldest first. [painFreeAllowed] is false
 * when an attack touches the day (BRD EC-DAY-01).
 */
@Parcelize
data class HistoryDayUiModel(
    val epochDay: Long,
    val sky: DaySkyUiModel,
    val peak: PeakUiModel?,
    val attacks: List<HistoryAttackUiModel>,
    val period: Boolean = false,
    val painFree: Boolean = false,
    val painFreeAllowed: Boolean = true
) : Parcelable
