package com.migrely.app.home.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TodayUiModel(
    val sky: SkyUiModel,
    val attackCount: Int,
    val peak: PeakUiModel?,
    val attacks: List<AttackSummaryUiModel> = emptyList(),
    val periodToday: Boolean = false,
    val periodOffered: Boolean = true
) : Parcelable
