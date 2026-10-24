package com.migrely.app.home.presentation.model

/** [peak] is null when no attack touched today. [attacks] lists today's ended attacks, oldest first. */
data class TodayPresentationModel(
    val sky: SkyPresentationModel,
    val attackCount: Int,
    val peak: PeakPresentationModel?,
    val attacks: List<AttackSummaryPresentationModel> = emptyList()
)
