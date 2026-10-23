package com.migrely.app.home.domain.model

sealed interface AnswerLongAttackResultDomainModel {
    data object Saved : AnswerLongAttackResultDomainModel

    data object NotInProgress : AnswerLongAttackResultDomainModel

    data object StorageFull : AnswerLongAttackResultDomainModel

    data object Failed : AnswerLongAttackResultDomainModel
}
