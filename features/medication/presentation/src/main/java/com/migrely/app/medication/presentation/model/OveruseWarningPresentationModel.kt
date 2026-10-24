package com.migrely.app.medication.presentation.model

/** [days] of the last 30; [thresholdDays] is the category's limit (BRD D45). */
data class OveruseWarningPresentationModel(
    val category: OveruseCategoryPresentationModel,
    val level: OveruseLevelPresentationModel,
    val days: Int,
    val thresholdDays: Int
)
