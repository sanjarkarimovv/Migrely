package com.migrely.app.start

import com.migrely.app.onboarding.domain.usecase.ShouldShowOnboardingUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** What `MainActivity` needs before its first frame, to choose where the app starts (BRD D76). */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface StartEntryPoint {
    fun shouldShowOnboardingUseCase(): ShouldShowOnboardingUseCase
}
