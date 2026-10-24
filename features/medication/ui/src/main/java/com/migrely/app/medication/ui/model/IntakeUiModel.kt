package com.migrely.app.medication.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class IntakeUiModel(
    val id: String,
    val medicationName: String,
    val dose: String?,
    val takenAtEpochMillis: Long,
    val zoneId: String,
    val attackId: String?
) : Parcelable
