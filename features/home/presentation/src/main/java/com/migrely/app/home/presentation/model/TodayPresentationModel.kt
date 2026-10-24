package com.migrely.app.home.presentation.model

/** [peak] is null when no attack touched today. */
data class TodayPresentationModel(
    val sky: SkyPresentationModel,
    val attackCount: Int,
    val peak: PeakPresentationModel?
)
