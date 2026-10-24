package com.migrely.app.datasource.medication.model

import java.time.Instant

/** The last time the user tapped "Got it" on a warning of this category and level (BRD EC-MED-06). */
data class OveruseNoticeDataModel(
    val category: OveruseCategoryDataModel,
    val level: OveruseLevelDataModel,
    val acknowledgedAt: Instant
)
