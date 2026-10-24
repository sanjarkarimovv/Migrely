package com.migrely.app.home.domain.model

sealed interface AttackLookupResultDomainModel {
    data class InProgress(val attack: AttackDomainModel) : AttackLookupResultDomainModel

    data object None : AttackLookupResultDomainModel

    data object Failed : AttackLookupResultDomainModel
}
