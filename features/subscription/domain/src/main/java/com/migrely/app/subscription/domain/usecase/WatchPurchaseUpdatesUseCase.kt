package com.migrely.app.subscription.domain.usecase

import com.migrely.app.architecture.domain.usecase.ContinuousExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.domain.repository.GetPurchaseUpdatesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Checks Google Play again each time it reports a purchase change outside a purchase sheet, such as
 * a pending payment that completed, so the purchase is acknowledged and Premium unlocks at once
 * (BRD EC-BIL-08, review I1). The status after each check comes out. Collected app-wide, for as
 * long as the app runs, never by a view model (ARCHITECTURE section 7, rule 6).
 */
class WatchPurchaseUpdatesUseCase(
    private val getPurchaseUpdatesRepository: GetPurchaseUpdatesRepository,
    private val premiumChecker: PremiumChecker,
    coroutineContextProvider: CoroutineContextProvider
) : ContinuousExecutingUseCase<Unit, PremiumStatusDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): Flow<PremiumStatusDomainModel> =
        getPurchaseUpdatesRepository.purchaseUpdates().map { premiumChecker.check().status }
}
