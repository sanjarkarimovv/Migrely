package com.migrely.app.attack.domain.repository

import com.migrely.app.attack.domain.model.OverlapLookupResultDomainModel
import java.time.Instant

interface FindOverlappingAttacksRepository {
    /**
     * Every attack other than [excludingId] that overlaps `(start, end)`, oldest first. An attack in progress counts as
     * open-ended; touching at one instant does not count.
     */
    fun overlappingAttacks(
        excludingId: String?,
        start: Instant,
        end: Instant
    ): OverlapLookupResultDomainModel
}
