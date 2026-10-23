package com.migrely.app.attack.domain.model

import java.time.Instant
import java.time.ZoneId

/**
 * One merge, ready to be written in one step: [survivorId] takes the new fields and the pain entries and tags of
 * [absorbedIds], which are then removed (BRD D27).
 */
data class AttackMergeDomainModel(
    val survivorId: String,
    val absorbedIds: List<String>,
    val start: Instant,
    val startZone: ZoneId,
    val end: Instant?,
    val functionalImpact: FunctionalImpactDomainModel?,
    val note: String
)
