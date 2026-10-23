package com.migrely.app.home.domain.model

sealed interface TodayDomainModel {
    data object Calm : TodayDomainModel

    data class AttackInProgress(val attack: AttackDomainModel, val longAttackCheckDue: Boolean) :
        TodayDomainModel

    /** [attacks] lists the ended attacks that touched today, oldest first (BRD 18.3 A). */
    data class AttacksEnded(
        val count: Int,
        val highestRecordedIntensity: Int?,
        val anyIntensityNotRecorded: Boolean,
        val attacks: List<AttackSummaryDomainModel> = emptyList()
    ) : TodayDomainModel
}
