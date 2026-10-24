package com.migrely.app.attack.domain.model

import java.time.Instant
import java.time.ZoneId

/**
 * A medication taken during the attack (BRD ATK-F5, MED-F3); [medicationName] stays for archived ones (EC-MED-01).
 * [effect] is how much it helped, if answered (D39).
 */
data class AttackIntakeDomainModel(
    val id: String,
    val medicationId: String,
    val medicationName: String,
    val dose: String?,
    val takenAt: Instant,
    val zone: ZoneId,
    val effect: AttackIntakeEffectDomainModel? = null
)
