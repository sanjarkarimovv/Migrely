package com.migrely.app.attack.domain.model

import java.time.Instant

/** An attack that has already ended (BRD ATK-F3). */
data class CreatePastAttackRequestDomainModel(val start: Instant, val end: Instant)
