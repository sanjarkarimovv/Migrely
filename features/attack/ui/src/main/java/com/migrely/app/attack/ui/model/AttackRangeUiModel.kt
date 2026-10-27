package com.migrely.app.attack.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** When another attack ran; [endEpochMillis] is null while it is in progress. */
@Parcelize
data class AttackRangeUiModel(val startEpochMillis: Long, val endEpochMillis: Long?) : Parcelable
