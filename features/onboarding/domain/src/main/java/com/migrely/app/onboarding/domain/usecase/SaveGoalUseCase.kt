package com.migrely.app.onboarding.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.onboarding.domain.model.OnboardingGoalDomainModel
import com.migrely.app.onboarding.domain.model.OnboardingWriteResultDomainModel
import com.migrely.app.onboarding.domain.repository.SetGoalRepository

/** Saves why the user came (BRD ONB-F1, D77). */
class SaveGoalUseCase(
    private val setGoalRepository: SetGoalRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<OnboardingGoalDomainModel, OnboardingWriteResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(
        request: OnboardingGoalDomainModel
    ): OnboardingWriteResultDomainModel = setGoalRepository.setGoal(request)
}
