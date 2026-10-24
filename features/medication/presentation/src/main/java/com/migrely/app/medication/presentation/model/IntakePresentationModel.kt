package com.migrely.app.medication.presentation.model

import java.time.Instant
import java.time.ZoneId

data class IntakePresentationModel(
    val id: String,
    val medicationName: String,
    val dose: String?,
    val takenAt: Instant,
    val zone: ZoneId,
    val attackId: String?
)
