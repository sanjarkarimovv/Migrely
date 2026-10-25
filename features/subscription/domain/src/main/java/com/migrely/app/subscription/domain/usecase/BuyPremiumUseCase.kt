package com.migrely.app.subscription.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.subscription.domain.model.BuyPremiumResultDomainModel
import com.migrely.app.subscription.domain.model.EntitlementKindDomainModel
import com.migrely.app.subscription.domain.model.LaunchedPurchaseDomainModel
import com.migrely.app.subscription.domain.model.PremiumPlanDomainModel
import com.migrely.app.subscription.domain.model.PremiumProductDomainModel
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.domain.model.PurchaseLaunchDomainModel
import com.migrely.app.subscription.domain.model.PurchaseOutcomeDomainModel
import com.migrely.app.subscription.domain.model.PurchaseSheetResultDomainModel
import com.migrely.app.subscription.domain.model.PurchaseStateDomainModel
import com.migrely.app.subscription.domain.repository.GetCurrentTimeRepository
import com.migrely.app.subscription.domain.repository.GetEntitlementRepository
import com.migrely.app.subscription.domain.repository.LaunchPurchaseRepository
import com.migrely.app.subscription.domain.repository.SaveLaunchedPurchaseRepository

/**
 * Buys [PremiumProductDomainModel] through Google Play's sheet (BRD D107, EC-BIL-08–10):
 * - a subscriber on a known plan choosing the other one upgrades it, prorated by time by Google
 *   Play; one whose plan is unknown here is offered lifetime only (review M9);
 * - what is asked of the sheet is kept before it opens, so a purchase is never left without its
 *   plan and trial if the app is closed while the sheet is open (review M5);
 * - after the sheet closes, however it closed, Google Play is asked again, so the status shown is
 *   always what Play holds: a completed purchase is checked, acknowledged and unlocks at once, a
 *   pending one unlocks nothing, and a cancelled sheet changes nothing;
 * - the plan and the trial of a purchase made here are kept, since Google Play does not report
 *   them.
 */
@Suppress("LongParameterList")
class BuyPremiumUseCase(
    private val getEntitlementRepository: GetEntitlementRepository,
    private val saveLaunchedPurchaseRepository: SaveLaunchedPurchaseRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val launchPurchaseRepository: LaunchPurchaseRepository,
    private val premiumChecker: PremiumChecker,
    private val entitlementRules: EntitlementRules,
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
                entitlement.plan != null &&
                request.plan != PremiumPlanDomainModel.Lifetime &&
                request.plan != entitlement.plan
        }
        val launched = LaunchedPurchaseDomainModel(
            plan = request.plan,
            freeTrial = request.freeTrial,
            at = getCurrentTimeRepository.now()
        )
        saveLaunchedPurchaseRepository.saveLaunchedPurchase(launched)
        val sheet = launchPurchaseRepository.launchPurchase(
            PurchaseLaunchDomainModel(request, replaced)
        )
        val returned = (sheet as? PurchaseSheetResultDomainModel.Completed)?.purchases.orEmpty()
        // Only a purchase made by this sheet takes its plan and trial: one already pending, or an
        // older one of the same product the sheet hands back, keeps what it had (walk B1).
        val bought = entitlementRules.boughtFrom(
            launched,
            returned,
            entitlement,
            getCurrentTimeRepository.now()
        )
        val status = premiumChecker.check(bought).status
        // The sheet answered, so the check above had what it needs; a late answer reads Play.
        saveLaunchedPurchaseRepository.saveLaunchedPurchase(null)
        val outcome = when (sheet) {
            is PurchaseSheetResultDomainModel.Completed -> when {
                status.unlocks() -> PurchaseOutcomeDomainModel.Unlocked
                returned.any {
                    it.product == entitlementRules.productOf(request.plan) &&
                        it.state == PurchaseStateDomainModel.Pending
                } ||
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

    private fun PremiumStatusDomainModel.unlocks() = when (this) {
        PremiumStatusDomainModel.Free, PremiumStatusDomainModel.Pending -> false
        is PremiumStatusDomainModel.Subscribed, is PremiumStatusDomainModel.Lifetime -> true
    }
}
