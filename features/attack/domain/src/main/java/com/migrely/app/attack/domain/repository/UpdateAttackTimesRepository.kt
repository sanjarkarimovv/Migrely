package com.migrely.app.attack.domain.repository

import com.migrely.app.attack.domain.model.IntensityPointDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import java.time.Instant

interface UpdateAttackTimesRepository {
    /** Stores the new times and removes [pointsToRemove] in one step (BRD EC-ATK-09). */
    fun updateAttackTimes(
        attackId: String,
        start: Instant,
        end: Instant?,
        pointsToRemove: List<IntensityPointDomainModel>
    ): WriteResultDomainModel
}
