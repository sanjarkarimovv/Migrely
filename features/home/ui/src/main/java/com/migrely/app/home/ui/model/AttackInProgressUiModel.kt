package com.migrely.app.home.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AttackInProgressUiModel(
    val id: String,
    val startEpochMillis: Long,
    val startZoneId: String,
    val currentLevel: IntensityLevelUiModel?,
    val longAttackCheckDue: Boolean
) : Parcelable
