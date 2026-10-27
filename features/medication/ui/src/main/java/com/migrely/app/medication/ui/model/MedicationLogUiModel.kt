package com.migrely.app.medication.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MedicationLogUiModel(
    val defaultTimeEpochMillis: Long,
    val zoneId: String,
    val attackId: String?,
    val medications: List<MedicationUiModel>
) : Parcelable
