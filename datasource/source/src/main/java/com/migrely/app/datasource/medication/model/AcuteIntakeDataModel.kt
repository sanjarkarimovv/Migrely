package com.migrely.app.datasource.medication.model

import java.time.Instant

/** When and where an acute medication was taken, and its category; null is "Not sure" (BRD MED-F5, D45). */
data class AcuteIntakeDataModel(
    val takenAt: Instant,
    val takenZoneId: String,
    val category: MedicationCategoryDataModel?
)
