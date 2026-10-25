package com.migrely.app.subscription.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.subscription.domain.model.PremiumCheckDomainModel

/** "Restore purchases": asks Google Play again now, on a new phone too (BRD EC-BIL-07, SUB-F2). */
class RestorePurchasesUseCase(
    private val premiumChecker: PremiumChecker,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, PremiumCheckDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): PremiumCheckDomainModel =
        premiumChecker.check()
}
