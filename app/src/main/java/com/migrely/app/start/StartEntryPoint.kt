package com.migrely.app.start

import com.migrely.app.onboarding.domain.usecase.ShouldShowOnboardingUseCase
import com.migrely.app.report.domain.usecase.CleanUpReportsUseCase
import com.migrely.app.settings.domain.usecase.GetThemeUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * What the app needs as it starts: where `MainActivity` begins (BRD D76), the theme it draws in and each change to it
 * (D94), and clearing what a cut-off report left (EC-REP-03).
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface StartEntryPoint {
    fun shouldShowOnboardingUseCase(): ShouldShowOnboardingUseCase

    fun cleanUpReportsUseCase(): CleanUpReportsUseCase

    fun getThemeUseCase(): GetThemeUseCase
}
