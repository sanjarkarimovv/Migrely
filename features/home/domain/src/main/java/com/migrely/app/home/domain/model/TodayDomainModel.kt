package com.migrely.app.home.domain.model

sealed interface TodayDomainModel {
    data object Calm : TodayDomainModel

    data class AttackInProgress(val attack: AttackDomainModel, val longAttackCheckDue: Boolean) :
        TodayDomainModel

    data class AttacksEnded(
        val count: Int,
        val highestRecordedIntensity: Int?,
        val anyIntensityNotRecorded: Boolean
    ) : TodayDomainModel
}
