package com.migrely.app.datasource.attack.model

import java.time.Instant

data class AttackDataModel(
    val id: String,
    val start: Instant,
    val startZoneId: String,
    val end: Instant?,
    val intensityPoints: List<IntensityPointDataModel>,
    val longCheckConfirmedAt: Instant?,
    val longCheckDisabled: Boolean
)
