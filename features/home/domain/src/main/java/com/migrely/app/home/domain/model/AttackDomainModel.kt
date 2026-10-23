package com.migrely.app.home.domain.model

import java.time.Instant
import java.time.ZoneId

data class AttackDomainModel(
    val id: String,
    val start: Instant,
    val startZone: ZoneId,
    val end: Instant?,
    val intensityPoints: List<IntensityPointDomainModel>,
    val longAttackCheck: LongAttackCheckDomainModel
) {
    val peakIntensity: Int? get() = intensityPoints.maxOfOrNull(IntensityPointDomainModel::value)

    val latestIntensityPoint: IntensityPointDomainModel?
        get() = intensityPoints.maxByOrNull(IntensityPointDomainModel::at)
}
