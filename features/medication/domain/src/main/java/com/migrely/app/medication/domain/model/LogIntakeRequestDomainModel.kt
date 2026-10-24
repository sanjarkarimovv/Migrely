package com.migrely.app.medication.domain.model

import java.time.Instant

/** [attackId] is the attack the screen was opened for, or null to link the attack containing [takenAt], if any. */
data class LogIntakeRequestDomainModel(
    val medicationId: String,
    val medicationName: String,
    val dose: String?,
    val attackId: String?,
    val takenAt: Instant
)
