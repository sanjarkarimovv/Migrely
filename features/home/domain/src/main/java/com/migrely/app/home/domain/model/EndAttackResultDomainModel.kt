package com.migrely.app.home.domain.model

sealed interface EndAttackResultDomainModel {
    data class Ended(val attackId: String) : EndAttackResultDomainModel

    data object NotInProgress : EndAttackResultDomainModel

    data object EndOutOfRange : EndAttackResultDomainModel

    data object StorageFull : EndAttackResultDomainModel

    data object Failed : EndAttackResultDomainModel
}
