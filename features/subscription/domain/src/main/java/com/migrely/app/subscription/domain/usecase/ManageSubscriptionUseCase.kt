package com.migrely.app.subscription.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.domain.repository.ManageSubscriptionRepository

/**
 * "Manage subscription": Google Play's page for the subscription, where it is cancelled, since the
 * app cannot cancel it (BRD SUB-F2, EC-BIL-09). The status is read again afterwards.
 */
class ManageSubscriptionUseCase(
    private val manageSubscriptionRepository: ManageSubscriptionRepository,
    private val premiumChecker: PremiumChecker,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, PremiumStatusDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): PremiumStatusDomainModel {
        manageSubscriptionRepository.manageSubscription()
        return premiumChecker.check().status
    }
}
