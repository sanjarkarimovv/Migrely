package com.migrely.app.subscription.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.subscription.domain.model.BoughtDomainModel
import com.migrely.app.subscription.domain.model.BuyPremiumResultDomainModel
import com.migrely.app.subscription.domain.model.EntitlementKindDomainModel
import com.migrely.app.subscription.domain.model.PremiumPlanDomainModel
import com.migrely.app.subscription.domain.model.PremiumProductDomainModel
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.domain.model.PurchaseLaunchDomainModel
import com.migrely.app.subscription.domain.model.PurchaseOutcomeDomainModel
import com.migrely.app.subscription.domain.model.PurchaseProductDomainModel
import com.migrely.app.subscription.domain.model.PurchaseSheetResultDomainModel
import com.migrely.app.subscription.domain.model.PurchaseStateDomainModel
import com.migrely.app.subscription.domain.repository.GetEntitlementRepository
import com.migrely.app.subscription.domain.repository.LaunchPurchaseRepository

/**
 * Buys [PremiumProductDomainModel] through Google Play's sheet (BRD D107, EC-BIL-08–10):
 * - a subscriber choosing the other plan upgrades it, prorated by time by Google Play;
 * - after the sheet closes, however it closed, Google Play is asked again, so the status shown is
 *   always what Play holds: a completed purchase is checked, acknowledged and unlocks at once, a
 *   pending one unlocks nothing, and a cancelled sheet changes nothing;
 * - the plan and the trial of a purchase made here are kept, since Google Play does not report
 *   them.
 */
class BuyPremiumUseCase(
    private val getEntitlementRepository: GetEntitlementRepository,
    private val launchPurchaseRepository: LaunchPurchaseRepository,
    private val premiumChecker: PremiumChecker,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<PremiumProductDomainModel, BuyPremiumResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(
        request: PremiumProductDomainModel
    ): BuyPremiumResultDomainModel {
        val entitlement = getEntitlementRepository.entitlement()
        val replaced = entitlement.purchaseToken.takeIf {
            entitlement.kind == EntitlementKindDomainModel.Subscription &&
                request.plan != PremiumPlanDomainModel.Lifetime &&
                request.plan != entitlement.plan
        }
        val sheet = launchPurchaseRepository.launchPurchase(
            PurchaseLaunchDomainModel(request, replaced)
        )
        val bought = (sheet as? PurchaseSheetResultDomainModel.Completed)?.purchases
            ?.firstOrNull { it.product == productOf(request.plan) }
        val status = premiumChecker.check(
            bought?.let { BoughtDomainModel(it.purchaseToken, request.plan, request.freeTrial) }
        ).status
        val outcome = when (sheet) {
            is PurchaseSheetResultDomainModel.Completed -> when {
                status.unlocks() -> PurchaseOutcomeDomainModel.Unlocked
                bought?.state == PurchaseStateDomainModel.Pending ||
                    status == PremiumStatusDomainModel.Pending -> PurchaseOutcomeDomainModel.Pending
                else -> PurchaseOutcomeDomainModel.Failed
            }

            PurchaseSheetResultDomainModel.AlreadyOwned -> if (status.unlocks()) {
                PurchaseOutcomeDomainModel.Unlocked
            } else {
                PurchaseOutcomeDomainModel.Failed
            }

            // Nothing to say about a sheet that never answered: the check above says what Play holds.
            PurchaseSheetResultDomainModel.Cancelled,
            PurchaseSheetResultDomainModel.Unanswered -> PurchaseOutcomeDomainModel.Cancelled
            PurchaseSheetResultDomainModel.Unavailable -> PurchaseOutcomeDomainModel.Unavailable
            PurchaseSheetResultDomainModel.Failed -> PurchaseOutcomeDomainModel.Failed
        }
        return BuyPremiumResultDomainModel(outcome, status)
    }

    private fun productOf(plan: PremiumPlanDomainModel) = when (plan) {
        PremiumPlanDomainModel.Yearly,
        PremiumPlanDomainModel.Monthly -> PurchaseProductDomainModel.Subscription
        PremiumPlanDomainModel.Lifetime -> PurchaseProductDomainModel.Lifetime
    }

    private fun PremiumStatusDomainModel.unlocks() = when (this) {
        PremiumStatusDomainModel.Free, PremiumStatusDomainModel.Pending -> false
        is PremiumStatusDomainModel.Subscribed, is PremiumStatusDomainModel.Lifetime -> true
    }
}
