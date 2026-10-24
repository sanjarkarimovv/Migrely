package com.migrely.app.attack.domain.model

import java.time.Instant

/** New times for an attack. [end] is ignored while the attack is in progress. */
data class UpdateAttackTimesRequestDomainModel(
    val attackId: String,
    val start: Instant,
    val end: Instant?
)
