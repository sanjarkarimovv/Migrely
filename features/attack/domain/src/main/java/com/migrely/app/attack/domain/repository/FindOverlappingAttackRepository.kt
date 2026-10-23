package com.migrely.app.attack.domain.repository

import com.migrely.app.attack.domain.model.OverlapLookupResultDomainModel
import java.time.Instant

interface FindOverlappingAttackRepository {
    /** Another attack, other than [attackId], that overlaps `(start, end)`; touching at one instant does not count. */
    fun overlappingAttack(
        attackId: String,
        start: Instant,
        end: Instant
    ): OverlapLookupResultDomainModel
}
