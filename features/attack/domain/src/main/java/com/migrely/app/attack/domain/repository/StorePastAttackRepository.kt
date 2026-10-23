package com.migrely.app.attack.domain.repository

import com.migrely.app.attack.domain.model.StorePastAttackResultDomainModel
import java.time.Instant
import java.time.ZoneId

interface StorePastAttackRepository {
    /** Stores an ended attack only when no attack overlaps it, checked in the same step (BRD EC-ATK-17). */
    fun storePastAttack(
        start: Instant,
        zone: ZoneId,
        end: Instant
    ): StorePastAttackResultDomainModel
}
