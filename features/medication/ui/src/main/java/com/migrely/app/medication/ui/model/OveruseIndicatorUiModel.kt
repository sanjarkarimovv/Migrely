package com.migrely.app.medication.ui.model

/** History's standing overuse indicator (BRD D59); no rows hides it. */
data class OveruseIndicatorUiModel(val rows: List<OveruseIndicatorRowUiModel>)

data class OveruseIndicatorRowUiModel(
    val category: OveruseCategoryUiModel,
    val days: Int,
    val limit: Int,
    val reached: Boolean
)
