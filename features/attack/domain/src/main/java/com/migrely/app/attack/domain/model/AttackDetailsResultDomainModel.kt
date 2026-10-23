package com.migrely.app.attack.domain.model

sealed interface AttackDetailsResultDomainModel {
    data class Loaded(val screen: AttackDetailsScreenDomainModel) : AttackDetailsResultDomainModel

    data object NotFound : AttackDetailsResultDomainModel

    data object Failed : AttackDetailsResultDomainModel
}
