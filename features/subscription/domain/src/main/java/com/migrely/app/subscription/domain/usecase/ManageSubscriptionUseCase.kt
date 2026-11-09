package com.migrely.app.subscription.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.subscription.domain.model.ManageSubscriptionResultDomainModel
import com.migrely.app.subscription.domain.model.NetworkStateDomainModel
import com.migrely.app.subscription.domain.repository.GetNetworkStateRepository
import com.migrely.app.subscription.domain.repository.ManageSubscriptionRepository

/**
 * "Manage subscription": Google Play's page for the subscription, where it is cancelled, since the
 * app cannot cancel it (BRD SUB-F2, EC-BIL-09). The status is read again afterwards. When no
 * network on the phone has internet, the page does not open (D166, EC-BIL-19).
 */
class ManageSubscriptionUseCase(
    private val getNetworkStateRepository: GetNetworkStateRepository,
    private val manageSubscriptionRepository: ManageSubscriptionRepository,
    private val premiumChecker: PremiumChecker,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, ManageSubscriptionResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(request: Unit): ManageSubscriptionResultDomainModel {
        if (getNetworkStateRepository.networkState() == NetworkStateDomainModel.Offline) {
            return ManageSubscriptionResultDomainModel.Offline(premiumChecker.kept())
        }
        manageSubscriptionRepository.manageSubscription()
        return ManageSubscriptionResultDomainModel.Opened(premiumChecker.check().status)
    }
}
