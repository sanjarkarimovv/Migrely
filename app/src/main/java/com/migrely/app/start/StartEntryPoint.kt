package com.migrely.app.start

import com.migrely.app.onboarding.domain.usecase.ShouldShowOnboardingUseCase
import com.migrely.app.report.domain.usecase.CleanUpReportsUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * What the app needs as it starts: where `MainActivity` begins (BRD D76), and clearing what a cut-off report left
 * (EC-REP-03).
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface StartEntryPoint {
    fun shouldShowOnboardingUseCase(): ShouldShowOnboardingUseCase

    fun cleanUpReportsUseCase(): CleanUpReportsUseCase
}
