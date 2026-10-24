package com.migrely.app.medication.domain.model

import java.time.Instant
import java.time.ZoneId

/** An acute intake waiting for "Did it help?" (BRD MED-F4, EC-MED-05). */
data class PendingEffectQuestionDomainModel(
    val intakeId: String,
    val medicationName: String,
    val dose: String?,
    val takenAt: Instant,
    val zone: ZoneId
)
