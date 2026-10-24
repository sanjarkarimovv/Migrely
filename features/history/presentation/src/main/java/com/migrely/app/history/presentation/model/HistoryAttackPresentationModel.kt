package com.migrely.app.history.presentation.model

import java.time.Instant
import java.time.ZoneId

/** [end] is null while the attack is in progress; [peakIntensity] is null when never recorded. */
data class HistoryAttackPresentationModel(
    val id: String,
    val start: Instant,
    val startZone: ZoneId,
    val end: Instant?,
    val peakIntensity: Int?
)
