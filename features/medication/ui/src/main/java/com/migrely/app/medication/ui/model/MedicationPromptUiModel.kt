package com.migrely.app.medication.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** Home's one medication card (BRD D46). */
sealed interface MedicationPromptUiModel : Parcelable {
    @Parcelize
    data object Hidden : MedicationPromptUiModel

    @Parcelize
    data class EffectQuestion(
        val intakeId: String,
        val medicationName: String,
        val dose: String?,
        val takenAtEpochMillis: Long,
        val zoneId: String
    ) : MedicationPromptUiModel

    @Parcelize
    data class OveruseWarning(
        val category: OveruseCategoryUiModel,
        val level: OveruseLevelUiModel,
        val days: Int,
        val thresholdDays: Int
    ) : MedicationPromptUiModel
}
