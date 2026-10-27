package com.migrely.app.attack.domain.model

sealed interface StorePastAttackResultDomainModel {
    data class Stored(val attackId: String) : StorePastAttackResultDomainModel

    /** Nothing was stored: another attack overlapped by the time it was written (BRD EC-ATK-17). */
    data object Overlapping : StorePastAttackResultDomainModel

    data object StorageFull : StorePastAttackResultDomainModel

    data object Failed : StorePastAttackResultDomainModel
}
