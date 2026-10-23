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

    /** The times overlap [attacks], oldest first (BRD EC-ATK-05, EC-ATK-15). */
    data class Overlaps(val attacks: List<AttackRangeDomainModel>) :
        UpdateAttackTimesResultDomainModel

    data object NotFound : UpdateAttackTimesResultDomainModel

    data object StorageFull : UpdateAttackTimesResultDomainModel

    data object Failed : UpdateAttackTimesResultDomainModel
}
