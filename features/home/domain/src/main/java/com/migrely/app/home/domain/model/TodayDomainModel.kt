package com.migrely.app.home.domain.model

sealed interface TodayDomainModel {
    data object Calm : TodayDomainModel

    /** The user marked today pain-free and no attack touches it (BRD EC-UX-09, EC-DAY-01, D49). */
    data object PainFree : TodayDomainModel

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
