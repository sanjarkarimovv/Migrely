package com.migrely.app.home.domain.model

sealed interface TodayResultDomainModel {
    /** [recentlyDeleted] is set while an attack deleted from its details screen can still be restored. */
    data class Loaded(
        val today: TodayDomainModel,
        val recentlyDeleted: DeletedAttackDomainModel? = null
    ) : TodayResultDomainModel

    data object Failed : TodayResultDomainModel
}
