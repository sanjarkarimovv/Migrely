package com.migrely.app.subscription.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.domain.model.RefreshPremiumRequestDomainModel

/**
 * The premium status, checked with Google Play at most every 15 minutes unless forced (BRD D106):
 * at start, on return, and wherever the status shows. Between checks it is the last one's answer.
 */
class RefreshPremiumUseCase(
    private val premiumChecker: PremiumChecker,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<RefreshPremiumRequestDomainModel, PremiumStatusDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(
        request: RefreshPremiumRequestDomainModel
    ): PremiumStatusDomainModel = if (request.force) {
        premiumChecker.check().status
    } else {
        premiumChecker.current()
    }
}
