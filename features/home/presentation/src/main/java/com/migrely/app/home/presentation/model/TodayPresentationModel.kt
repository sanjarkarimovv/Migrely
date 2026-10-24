package com.migrely.app.home.presentation.model

/**
 * [peak] is null when no attack touched today. [attacks] lists today's ended attacks, oldest first. [periodToday] is
 * today's period mark (BRD D48).
 */
data class TodayPresentationModel(
    val sky: SkyPresentationModel,
    val attackCount: Int,
    val peak: PeakPresentationModel?,
    val attacks: List<AttackSummaryPresentationModel> = emptyList(),
    val periodToday: Boolean = false
)
