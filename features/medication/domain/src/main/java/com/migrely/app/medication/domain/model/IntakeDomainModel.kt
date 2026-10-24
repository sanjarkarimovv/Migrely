package com.migrely.app.medication.domain.model

import java.time.Instant
import java.time.ZoneId

/** A logged intake (BRD MED-F3); [attackId] is null when it is not part of an attack. */
data class IntakeDomainModel(
    val id: String,
    val medicationId: String,
    val medicationName: String,
    val dose: String?,
    val takenAt: Instant,
    val zone: ZoneId,
    val attackId: String?
)
