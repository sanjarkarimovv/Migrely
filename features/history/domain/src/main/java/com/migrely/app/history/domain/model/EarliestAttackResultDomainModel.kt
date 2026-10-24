package com.migrely.app.history.domain.model

import java.time.Instant
import java.time.ZoneId

sealed interface EarliestAttackResultDomainModel {
    /** The earliest attack started at [start] in [zone]. */
    data class Found(val start: Instant, val zone: ZoneId) : EarliestAttackResultDomainModel

    data object None : EarliestAttackResultDomainModel

    data object Failed : EarliestAttackResultDomainModel
}
