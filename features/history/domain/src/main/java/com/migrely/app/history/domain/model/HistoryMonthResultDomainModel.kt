package com.migrely.app.history.domain.model

sealed interface HistoryMonthResultDomainModel {
    /** [recentlyDeleted] is set while an attack deleted from its details screen can still be restored. */
    data class Loaded(
        val month: HistoryMonthDomainModel,
        val recentlyDeleted: DeletedAttackDomainModel? = null
    ) : HistoryMonthResultDomainModel

    data object Failed : HistoryMonthResultDomainModel
}
