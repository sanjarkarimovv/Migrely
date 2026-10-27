package com.migrely.app.home.presentation.model

import java.time.Instant
import java.time.ZoneId

/** An ended attack listed under today's sky; [peakIntensity] is null when it was not recorded. */
data class AttackSummaryPresentationModel(
    val id: String,
    val start: Instant,
    val startZone: ZoneId,
    val end: Instant,
    val peakIntensity: Int?
)
