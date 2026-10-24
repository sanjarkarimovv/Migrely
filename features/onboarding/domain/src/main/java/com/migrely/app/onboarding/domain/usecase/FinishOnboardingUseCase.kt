package com.migrely.app.onboarding.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.onboarding.domain.model.OnboardingWriteResultDomainModel
import com.migrely.app.onboarding.domain.repository.FinishOnboardingRepository

/** Onboarding was finished or skipped to the end, so it never shows again (BRD D76). */
class FinishOnboardingUseCase(
    private val finishOnboardingRepository: FinishOnboardingRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, OnboardingWriteResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): OnboardingWriteResultDomainModel =
        finishOnboardingRepository.finishOnboarding()
}
