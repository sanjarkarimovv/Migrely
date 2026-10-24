package com.migrely.app.attack.domain.model

import java.time.Instant

sealed interface UpdateAttackTimesResultDomainModel {
    /** Stored. The previous times and [removedPoints] (entries left outside, EC-ATK-09) allow an undo. */
    data class Updated(
        val previousStart: Instant,
        val previousEnd: Instant?,
        val removedPoints: List<IntensityPointDomainModel>
    ) : UpdateAttackTimesResultDomainModel

    data class InFuture(val field: TimeFieldDomainModel) : UpdateAttackTimesResultDomainModel

    data object EndBeforeStart : UpdateAttackTimesResultDomainModel

    data class Overlaps(val otherStart: Instant, val otherEnd: Instant?) :
        UpdateAttackTimesResultDomainModel

    data object NotFound : UpdateAttackTimesResultDomainModel

    data object StorageFull : UpdateAttackTimesResultDomainModel

    data object Failed : UpdateAttackTimesResultDomainModel
}
