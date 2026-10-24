package com.migrely.app.datasource.medication.model

import java.time.Instant

/** A medication taken; [medicationName] is the medication's current name, archived or not. */
data class IntakeDataModel(
    val id: String,
    val medicationId: String,
    val medicationName: String,
    val attackId: String?,
    val takenAt: Instant,
    val takenZoneId: String,
    val dose: String?
)
