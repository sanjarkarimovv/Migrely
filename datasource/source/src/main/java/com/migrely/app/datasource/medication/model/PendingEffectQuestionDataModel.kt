package com.migrely.app.datasource.medication.model

import java.time.Instant

/** An acute intake still waiting for "Did it help?" (BRD MED-F4, EC-MED-05). */
data class PendingEffectQuestionDataModel(
    val intakeId: String,
    val medicationName: String,
    val dose: String?,
    val takenAt: Instant,
    val takenZoneId: String
)
