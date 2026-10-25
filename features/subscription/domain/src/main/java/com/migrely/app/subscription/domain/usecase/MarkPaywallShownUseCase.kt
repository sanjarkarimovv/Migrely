package com.migrely.app.subscription.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.subscription.domain.model.PaywallTriggerDomainModel
import com.migrely.app.subscription.domain.repository.GetCurrentTimeRepository
import com.migrely.app.subscription.domain.repository.MarkPaywallShownRepository

/**
 * Records that a paywall showed itself for the trigger, and when, as it appears (design D5). A
 * write that fails is not retried: at worst the paywall can show itself once more.
 */
class MarkPaywallShownUseCase(
    private val markPaywallShownRepository: MarkPaywallShownRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<PaywallTriggerDomainModel, Unit>(coroutineContextProvider) {
    override fun executeInBackground(request: PaywallTriggerDomainModel) {
        markPaywallShownRepository.markPaywallShown(request, getCurrentTimeRepository.now())
    }
}
