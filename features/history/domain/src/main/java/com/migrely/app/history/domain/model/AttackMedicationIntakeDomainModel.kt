package com.migrely.app.history.domain.model

import java.time.Instant
import java.time.ZoneId

/** An intake of a medication for attacks, with the zone it was taken in (BRD D45). */
data class AttackMedicationIntakeDomainModel(val takenAt: Instant, val zone: ZoneId)
