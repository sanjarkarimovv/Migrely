package com.migrely.app.history.domain.model

import java.time.Instant
import java.time.ZoneId

/** An attack as History shows it. [end] is null while it is in progress; [peakIntensity] is null when never recorded. */
data class HistoryAttackDomainModel(
    val id: String,
    val start: Instant,
    val startZone: ZoneId,
    val end: Instant?,
    val peakIntensity: Int?
)
