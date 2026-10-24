package com.migrely.app.medication.domain.model

sealed interface PendingEffectQuestionResultDomainModel {
    data class Found(val question: PendingEffectQuestionDomainModel) :
        PendingEffectQuestionResultDomainModel

    data object None : PendingEffectQuestionResultDomainModel

    data object Failed : PendingEffectQuestionResultDomainModel
}
