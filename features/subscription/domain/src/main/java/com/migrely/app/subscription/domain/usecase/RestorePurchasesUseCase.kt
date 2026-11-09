package com.migrely.app.subscription.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.subscription.domain.model.NetworkStateDomainModel
import com.migrely.app.subscription.domain.model.RestorePurchasesResultDomainModel
import com.migrely.app.subscription.domain.repository.GetNetworkStateRepository

/**
 * "Restore purchases": asks Google Play again now, on a new phone too (BRD EC-BIL-07, SUB-F2).
 * When no network on the phone has internet, Play is not asked, so the answer is never "no
 * purchases were found" for a phone that only is offline (D166, EC-BIL-19).
 */
class RestorePurchasesUseCase(
    private val getNetworkStateRepository: GetNetworkStateRepository,
    private val premiumChecker: PremiumChecker,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, RestorePurchasesResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): RestorePurchasesResultDomainModel =
        if (getNetworkStateRepository.networkState() == NetworkStateDomainModel.Offline) {
            RestorePurchasesResultDomainModel.Offline(premiumChecker.kept())
        } else {
            RestorePurchasesResultDomainModel.Checked(premiumChecker.check())
        }
}
