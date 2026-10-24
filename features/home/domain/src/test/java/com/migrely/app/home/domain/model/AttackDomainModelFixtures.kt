package com.migrely.app.home.domain.model

import java.time.Instant
import java.time.ZoneId

internal val TASHKENT: ZoneId = ZoneId.of("Asia/Tashkent")

internal fun attack(
    id: String = "attack-1",
    start: Instant = Instant.parse("2026-10-23T09:00:00Z"),
    end: Instant? = null,
    startZone: ZoneId = TASHKENT,
    intensityPoints: List<IntensityPointDomainModel> = emptyList(),
    longAttackCheck: LongAttackCheckDomainModel =
        LongAttackCheckDomainModel(confirmedAt = null, disabled = false)
) = AttackDomainModel(
    id = id,
    start = start,
    startZone = startZone,
    end = end,
    intensityPoints = intensityPoints,
    longAttackCheck = longAttackCheck
)
