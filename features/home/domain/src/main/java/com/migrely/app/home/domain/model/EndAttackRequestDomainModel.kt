package com.migrely.app.home.domain.model

import java.time.Instant

/** [end] is null to end the attack now. */
data class EndAttackRequestDomainModel(val end: Instant?)
