package com.migrely.app.home.domain.model

import java.time.Instant
import java.time.ZoneId

/** One ended attack listed under today's sky; [peakIntensity] is null when no intensity was recorded. */
data class AttackSummaryDomainModel(
    val id: String,
    val start: Instant,
    val startZone: ZoneId,
    val end: Instant,
    val peakIntensity: Int?
)
