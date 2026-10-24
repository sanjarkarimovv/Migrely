package com.migrely.app.attack.domain.repository

import com.migrely.app.attack.domain.model.IntensityPointDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import java.time.Instant

interface RestoreAttackTimesRepository {
    /** Puts back earlier times and the intensity points a time change removed, in one step. */
    fun restoreAttackTimes(
        attackId: String,
        start: Instant,
        end: Instant?,
        points: List<IntensityPointDomainModel>
    ): WriteResultDomainModel
}
