package com.migrely.app.medication.presentation.model

/** One category's days in the last 30 days against its [limit]; [reached] from the limit on (BRD D59). */
data class OveruseIndicatorRowPresentationModel(
    val category: OveruseCategoryPresentationModel,
    val days: Int,
    val limit: Int,
    val reached: Boolean
)
