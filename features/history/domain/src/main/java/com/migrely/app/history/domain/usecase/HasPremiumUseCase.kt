package com.migrely.app.history.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.history.domain.repository.HasPremiumRepository

/**
 * Whether the doctor report and Insights open, or their paywall (BRD §7.1, D108). Both entries stay
 * on the screen either way.
 */
class HasPremiumUseCase(
    private val hasPremiumRepository: HasPremiumRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, Boolean>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): Boolean = hasPremiumRepository.hasPremium()
}
