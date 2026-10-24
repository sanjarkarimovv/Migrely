package com.migrely.app.attack.domain.model

import java.time.Instant

/** When an attack ran; [end] is null while it is in progress. */
data class AttackRangeDomainModel(val start: Instant, val end: Instant?)
