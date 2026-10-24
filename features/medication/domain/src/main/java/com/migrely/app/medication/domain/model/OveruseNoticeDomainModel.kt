package com.migrely.app.medication.domain.model

import java.time.Instant

/** The last "Got it" on a warning of this category and level (BRD EC-MED-06). */
data class OveruseNoticeDomainModel(
    val category: OveruseCategoryDomainModel,
    val level: OveruseLevelDomainModel,
    val acknowledgedAt: Instant
)
