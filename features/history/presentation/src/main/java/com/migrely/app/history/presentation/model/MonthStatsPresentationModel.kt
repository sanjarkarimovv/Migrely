package com.migrely.app.history.presentation.model

/** A month's numbers (BRD D53–D56); [averagePeak] is null when no attack has a pain level. */
data class MonthStatsPresentationModel(
    val migraines: Int,
    val migraineDays: Int,
    val painFreeDays: Int,
    val averagePeak: IntensityLevelPresentationModel?,
    val attackMedicationDays: Int,
    val chronic: Boolean,
    val progress: ProgressPresentationModel?
)
