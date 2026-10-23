package com.migrely.app.home.domain.repository

import com.migrely.app.home.domain.model.StartAttackResultDomainModel
import java.time.Instant
import java.time.ZoneId

interface StartAttackRepository {
    /** Must be atomic: never creates a second attack in progress (BRD EC-ATK-01). */
    fun startAttackIfNoneInProgress(start: Instant, zone: ZoneId): StartAttackResultDomainModel
}
