package com.migrely.app.home.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** An ended attack listed under today's sky; [peakIntensity] is null when it was not recorded. */
@Parcelize
data class AttackSummaryUiModel(
    val id: String,
    val startEpochMillis: Long,
    val startZoneId: String,
    val endEpochMillis: Long,
    val peakIntensity: Int?
) : Parcelable
