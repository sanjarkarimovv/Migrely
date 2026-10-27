package com.migrely.app.medication.domain.model

import java.time.Instant
import java.time.ZoneId

/** An acute intake for counting overuse days; [category] is null for "Not sure" (BRD EC-MED-04). */
data class AcuteIntakeDomainModel(
    val takenAt: Instant,
    val zone: ZoneId,
    val category: MedicationCategoryDomainModel?
)
