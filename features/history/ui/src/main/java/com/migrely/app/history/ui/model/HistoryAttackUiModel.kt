package com.migrely.app.history.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** [endEpochMillis] is null while the attack is in progress; [peakIntensity] is null when never recorded. */
@Parcelize
data class HistoryAttackUiModel(
    val id: String,
    val startEpochMillis: Long,
    val startZoneId: String,
    val endEpochMillis: Long?,
    val peakIntensity: Int?
) : Parcelable
