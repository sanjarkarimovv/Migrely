package com.migrely.app.subscription.domain.usecase

import com.migrely.app.subscription.domain.model.BillingAvailabilityDomainModel
import com.migrely.app.subscription.domain.model.BoughtDomainModel
import com.migrely.app.subscription.domain.model.OwnedPurchaseDomainModel
import com.migrely.app.subscription.domain.model.OwnedPurchasesResultDomainModel
import com.migrely.app.subscription.domain.model.PremiumCheckDomainModel
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.domain.model.PurchaseStateDomainModel
import com.migrely.app.subscription.domain.repository.AcknowledgePurchaseRepository
import com.migrely.app.subscription.domain.repository.GetCurrentTimeRepository
import com.migrely.app.subscription.domain.repository.GetEntitlementRepository
import com.migrely.app.subscription.domain.repository.GetOwnedPurchasesRepository
import com.migrely.app.subscription.domain.repository.SaveEntitlementRepository
import com.migrely.app.subscription.domain.repository.SchedulePremiumChecksRepository

/**
 * Asks Google Play what this account owns, applies [EntitlementRules] and keeps the result (BRD
 * D106). A purchase that unlocks and is not yet acknowledged is acknowledged, and asked about again
 * at the next check when that fails. The check that every use case about the entitlement shares.
 * One for the app: checks run one at a time, so two that overlap never write over each other's
 * answer, and a purchase's plan and trial are never lost to a check that read the cache before it
 * (review M5).
 */
class PremiumChecker(
    private val getEntitlementRepository: GetEntitlementRepository,
    private val saveEntitlementRepository: SaveEntitlementRepository,
    private val getOwnedPurchasesRepository: GetOwnedPurchasesRepository,
    private val acknowledgePurchaseRepository: AcknowledgePurchaseRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val schedulePremiumChecksRepository: SchedulePremiumChecksRepository,
    private val entitlementRules: EntitlementRules
) {
    private val lock = Any()

    /** The status, asking Google Play only when the last answer is old (BRD D106). */
    fun current(): PremiumStatusDomainModel = synchronized(lock) {
        val cached = getEntitlementRepository.entitlement()
        val now = getCurrentTimeRepository.now()
        if (entitlementRules.isDue(cached, now)) {
            check().status
        } else {
            entitlementRules.status(cached, now)
        }
    }

    /**
     * Asks Google Play now. [bought] is a purchase just made here, whose plan and trial are kept.
     */
    fun check(bought: BoughtDomainModel? = null): PremiumCheckDomainModel = synchronized(lock) {
        val previous = getEntitlementRepository.entitlement()
        val now = getCurrentTimeRepository.now()
        val owned = getOwnedPurchasesRepository.ownedPurchases()
        val entitlement = when (owned) {
            is OwnedPurchasesResultDomainModel.Owned -> entitlementRules.answered(
                previous,
                acknowledged(owned.purchases),
                now,
                bought
            )

            OwnedPurchasesResultDomainModel.Unavailable,
            OwnedPurchasesResultDomainModel.Unreachable ->
                entitlementRules.unanswered(previous, now)
        }
        // A write that fails leaves the last entitlement, and the next check tries again.
        if (entitlement != previous) saveEntitlementRepository.saveEntitlement(entitlement)
        schedulePremiumChecksRepository.schedulePremiumChecks(
            entitlementRules.needsBackgroundChecks(entitlement)
        )
        PremiumCheckDomainModel(
            status = entitlementRules.status(entitlement, now),
            billing = when (owned) {
                is OwnedPurchasesResultDomainModel.Owned -> BillingAvailabilityDomainModel.Available
                OwnedPurchasesResultDomainModel.Unavailable ->
                    BillingAvailabilityDomainModel.Unavailable
                OwnedPurchasesResultDomainModel.Unreachable ->
                    BillingAvailabilityDomainModel.Unknown
            }
        )
    }

    /** The purchases, with those that unlock acknowledged now where Google Play took it. */
    private fun acknowledged(purchases: List<OwnedPurchaseDomainModel>) = purchases.map {
        val due = it.state == PurchaseStateDomainModel.Purchased && it.signatureValid &&
            !it.acknowledged
        if (due && acknowledgePurchaseRepository.acknowledgePurchase(it.purchaseToken)) {
            it.copy(acknowledged = true)
        } else {
            it
        }
    }
}
