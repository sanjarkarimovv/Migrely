package com.migrely.app.home.presentation.model

import java.time.Instant
import java.time.ZoneId

data class AttackInProgressPresentationModel(
    val id: String,
    val start: Instant,
    val startZone: ZoneId,
    val currentLevel: IntensityLevelPresentationModel?,
    val longAttackCheckDue: Boolean
)
