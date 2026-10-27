package com.migrely.app.history.domain.model

sealed interface AttacksTouchingResultDomainModel {
    data class Loaded(val attacks: List<HistoryAttackDomainModel>) :
        AttacksTouchingResultDomainModel

    data object Failed : AttacksTouchingResultDomainModel
}
