package com.migrely.app.home.domain.model

sealed interface StartAttackResultDomainModel {
    data class Started(val attack: AttackDomainModel) : StartAttackResultDomainModel

    data class AlreadyInProgress(val attack: AttackDomainModel) : StartAttackResultDomainModel

    data object StorageFull : StartAttackResultDomainModel

    data object Failed : StartAttackResultDomainModel
}
