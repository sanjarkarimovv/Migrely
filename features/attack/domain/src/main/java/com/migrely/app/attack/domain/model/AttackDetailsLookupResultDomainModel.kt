package com.migrely.app.attack.domain.model

sealed interface AttackDetailsLookupResultDomainModel {
    data class Found(val details: AttackDetailsDomainModel) : AttackDetailsLookupResultDomainModel

    data object NotFound : AttackDetailsLookupResultDomainModel

    data object Failed : AttackDetailsLookupResultDomainModel
}
