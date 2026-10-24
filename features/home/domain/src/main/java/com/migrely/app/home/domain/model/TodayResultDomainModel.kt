package com.migrely.app.home.domain.model

sealed interface TodayResultDomainModel {
    /**
     * [recentlyDeleted] is set while an attack deleted from its details screen can still be restored. [periodToday] is
     * today's period mark (BRD D48).
     */
    data class Loaded(
        val today: TodayDomainModel,
        val recentlyDeleted: DeletedAttackDomainModel? = null,
        val periodToday: Boolean = false
    ) : TodayResultDomainModel

    data object Failed : TodayResultDomainModel
}
