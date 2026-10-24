package com.migrely.app.history.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** A month's numbers (BRD D53–D56); [averagePeak] is null when no attack has a pain level. */
@Parcelize
data class MonthStatsUiModel(
    val migraines: Int,
    val migraineDays: Int,
    val painFreeDays: Int,
    val averagePeak: PeakUiModel?,
    val attackMedicationDays: Int,
    val chronic: Boolean,
    val progress: ProgressUiModel?
) : Parcelable
