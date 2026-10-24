package com.migrely.app.home.domain.model

import java.time.Instant

data class TodaySnapshotDomainModel(
    val attackInProgress: AttackDomainModel?,
    val endedAttacksTouchingToday: List<AttackDomainModel>,
    val now: Instant
)
