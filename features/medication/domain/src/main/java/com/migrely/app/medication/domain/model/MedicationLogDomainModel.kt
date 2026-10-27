package com.migrely.app.medication.domain.model

import java.time.Instant
import java.time.ZoneId

/**
 * What the "Medication taken" screen starts with (BRD D34): the default time and the zone it is shown and picked in,
 * the attack it logs for, if any, and the active medications, acute first, then most recently used, then by name.
 */
data class MedicationLogDomainModel(
    val defaultTime: Instant,
    val zone: ZoneId,
    val attackId: String?,
    val medications: List<MedicationDomainModel>
)
