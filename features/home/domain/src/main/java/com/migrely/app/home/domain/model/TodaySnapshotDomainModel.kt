package com.migrely.app.home.domain.model

/** What home reads for today; [endedAttacks] also holds attacks of nearby days, which the use case filters out. */
data class TodaySnapshotDomainModel(
    val attackInProgress: AttackDomainModel?,
    val endedAttacks: List<AttackDomainModel>,
    val recentlyDeleted: DeletedAttackDomainModel? = null
)
