package com.migrely.app.attack.domain.model

import java.time.Instant

/** The details screen's data: the attack, the user's active custom tags of each kind, and the time it was read. */
data class AttackDetailsScreenDomainModel(
    val details: AttackDetailsDomainModel,
    val customSymptoms: List<CustomTagDomainModel>,
    val customTriggers: List<CustomTagDomainModel>,
    val now: Instant
)
