package com.migrely.app.subscription.domain.usecase

import com.migrely.app.subscription.domain.model.BillingAvailabilityDomainModel
import com.migrely.app.subscription.domain.model.BoughtDomainModel
import com.migrely.app.subscription.domain.model.EntitlementDomainModel
import com.migrely.app.subscription.domain.model.OwnedPurchaseDomainModel
import com.migrely.app.subscription.domain.model.OwnedPurchasesResultDomainModel
import com.migrely.app.subscription.domain.model.PremiumCheckDomainModel
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.domain.model.PurchaseStateDomainModel
import com.migrely.app.subscription.domain.model.SubscriptionWriteResultDomainModel
import com.migrely.app.subscription.domain.repository.AcknowledgePurchaseRepository
import com.migrely.app.subscription.domain.repository.GetCurrentTimeRepository
import com.migrely.app.subscription.domain.repository.GetEntitlementRepository
import com.migrely.app.subscription.domain.repository.GetLaunchedPurchaseRepository
import com.migrely.app.subscription.domain.repository.GetOwnedPurchasesRepository
import com.migrely.app.subscription.domain.repository.SaveEntitlementRepository
import com.migrely.app.subscription.domain.repository.SaveLaunchedPurchaseRepository
import com.migrely.app.subscription.domain.repository.SchedulePremiumChecksRepository
import java.time.Instant

/**
 * Asks Google Play what this account owns, applies [EntitlementRules] and keeps the result (BRD
 * D106). A purchase that unlocks and is not yet acknowledged is acknowledged, and asked about again
 * at the next check when that fails. The check that every use case about the entitlement shares.
 * One for the app: checks run one at a time, so two that overlap never write over each other's
 * answer, and a purchase's plan and trial are never lost to a check that read the cache before it
 * (review M5).
 */
@Suppress("LongParameterList")
class PremiumChecker(
    private val getEntitlementRepository: GetEntitlementRepository,
    private val saveEntitlementRepository: SaveEntitlementRepository,
    private val getOwnedPurchasesRepository: GetOwnedPurchasesRepository,
    private val acknowledgePurchaseRepository: AcknowledgePurchaseRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val schedulePremiumChecksRepository: SchedulePremiumChecksRepository,
    private val getLaunchedPurchaseRepository: GetLaunchedPurchaseRepository,
    private val saveLaunchedPurchaseRepository: SaveLaunchedPurchaseRepository,
    private val entitlementRules: EntitlementRules
) {
    private val lock = Any()

    /**
     * The status, asking Google Play only when the last answer is old, or when a purchase sheet
     * opened since then never answered (BRD D106, EC-BIL-13).
     */
    fun current(): PremiumStatusDomainModel = synchronized(lock) {
        val cached = getEntitlementRepository.entitlement()
        val now = getCurrentTimeRepository.now()
        if (entitlementRules.isDue(cached, now, getLaunchedPurchaseRepository.launchedPurchase())) {
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
        var boughtAfterLaunch: BoughtDomainModel? = null
        val entitlement = when (owned) {
            is OwnedPurchasesResultDomainModel.Owned -> {
                val purchases = acknowledged(owned.purchases)
                if (bought == null) boughtAfterLaunch = boughtAfterLaunch(purchases, previous, now)
                entitlementRules.answered(previous, purchases, now, bought ?: boughtAfterLaunch)
            }

            OwnedPurchasesResultDomainModel.Unavailable,
            OwnedPurchasesResultDomainModel.Unreachable ->
                entitlementRules.unanswered(previous, now)
        }
        // A write that fails leaves the last entitlement, and the next check tries again; so a
        // launch is forgotten only once its purchase's plan and trial are written.
        val written = entitlement == previous ||
            saveEntitlementRepository.saveEntitlement(entitlement) ==
            SubscriptionWriteResultDomainModel.Done
        if (written && boughtAfterLaunch != null) {
            saveLaunchedPurchaseRepository.saveLaunchedPurchase(null)
        }
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

    /**
     * A purchase whose sheet was launched here but whose answer never came back, for example
     * because the app was closed meanwhile (review M5).
     */
    private fun boughtAfterLaunch(
        purchases: List<OwnedPurchaseDomainModel>,
        previous: EntitlementDomainModel,
        now: Instant
    ): BoughtDomainModel? {
        val launched = getLaunchedPurchaseRepository.launchedPurchase() ?: return null
        return entitlementRules.boughtFrom(launched, purchases, previous, now)
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
