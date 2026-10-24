package com.migrely.app.history.presentation.model

import java.time.LocalDate

/**
 * One calendar day. [peak] is null when nothing touched the day; [attacks] are oldest first. [painFreeAllowed] is false
 * when an attack touches the day (BRD EC-DAY-01).
 */
data class HistoryDayPresentationModel(
    val date: LocalDate,
    val sky: DaySkyPresentationModel,
    val peak: PeakPresentationModel?,
    val attacks: List<HistoryAttackPresentationModel>,
    val period: Boolean = false,
    val painFree: Boolean = false,
    val painFreeAllowed: Boolean = true
)
