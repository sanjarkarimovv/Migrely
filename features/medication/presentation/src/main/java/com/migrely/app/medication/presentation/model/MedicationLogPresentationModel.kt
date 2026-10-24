package com.migrely.app.medication.presentation.model

import java.time.Instant
import java.time.ZoneId

data class MedicationLogPresentationModel(
    val defaultTime: Instant,
    val zone: ZoneId,
    val attackId: String?,
    val medications: List<MedicationPresentationModel>
)
