package com.migrely.app.onboarding.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.onboarding.domain.model.OnboardingWriteResultDomainModel
import com.migrely.app.onboarding.domain.repository.SetPeriodTrackingRepository

/** Turns "Track periods" on or off from onboarding's third step (BRD DAY-F1, D79). */
class SavePeriodTrackingUseCase(
    private val setPeriodTrackingRepository: SetPeriodTrackingRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Boolean, OnboardingWriteResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(request: Boolean): OnboardingWriteResultDomainModel =
        setPeriodTrackingRepository.setPeriodTracking(request)
}
