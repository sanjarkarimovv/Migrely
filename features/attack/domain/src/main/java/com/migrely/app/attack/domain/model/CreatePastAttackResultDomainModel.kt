package com.migrely.app.attack.domain.model

sealed interface CreatePastAttackResultDomainModel {
    data class Created(val attackId: String) : CreatePastAttackResultDomainModel

    data class InFuture(val field: TimeFieldDomainModel) : CreatePastAttackResultDomainModel

    data object EndBeforeStart : CreatePastAttackResultDomainModel

    /** Nothing was stored: the times overlap [attacks], oldest first (BRD EC-ATK-05, EC-ATK-15). */
    data class Overlaps(val attacks: List<AttackRangeDomainModel>) :
        CreatePastAttackResultDomainModel

    data object StorageFull : CreatePastAttackResultDomainModel

    data object Failed : CreatePastAttackResultDomainModel
}
