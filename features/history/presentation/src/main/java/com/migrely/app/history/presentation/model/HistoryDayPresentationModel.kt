package com.migrely.app.history.presentation.model

import java.time.LocalDate

/** One calendar day. [peak] is null when nothing touched the day; [attacks] are oldest first. */
data class HistoryDayPresentationModel(
    val date: LocalDate,
    val sky: DaySkyPresentationModel,
    val peak: PeakPresentationModel?,
    val attacks: List<HistoryAttackPresentationModel>
)
