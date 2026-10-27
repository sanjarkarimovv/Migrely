package com.migrely.app.medication.domain.model

sealed interface AttackLookupResultDomainModel {
    data class Found(val attack: LinkedAttackDomainModel) : AttackLookupResultDomainModel

    data object None : AttackLookupResultDomainModel

    data object Failed : AttackLookupResultDomainModel
}
