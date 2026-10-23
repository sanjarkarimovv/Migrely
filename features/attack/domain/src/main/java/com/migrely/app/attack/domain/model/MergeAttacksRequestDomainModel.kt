package com.migrely.app.attack.domain.model

import java.time.Instant

/**
 * Times to merge with the attacks they overlap. [attackId] is the attack being edited, or null for a new past attack,
 * which always has an [end]. The end of an attack in progress is ignored.
 */
data class MergeAttacksRequestDomainModel(
    val attackId: String?,
    val start: Instant,
    val end: Instant?
)
