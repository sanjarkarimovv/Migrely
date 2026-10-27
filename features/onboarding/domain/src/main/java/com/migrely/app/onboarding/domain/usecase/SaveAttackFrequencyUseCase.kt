package com.migrely.app.onboarding.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.onboarding.domain.model.AttackFrequencyDomainModel
import com.migrely.app.onboarding.domain.model.OnboardingWriteResultDomainModel
import com.migrely.app.onboarding.domain.repository.SetAttackFrequencyRepository

/** Saves how often migraines come (BRD ONB-F1, D77). */
class SaveAttackFrequencyUseCase(
    private val setAttackFrequencyRepository: SetAttackFrequencyRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<AttackFrequencyDomainModel, OnboardingWriteResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(
        request: AttackFrequencyDomainModel
    ): OnboardingWriteResultDomainModel = setAttackFrequencyRepository.setAttackFrequency(request)
}
