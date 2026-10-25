package com.migrely.app.subscription.domain.usecase

import com.migrely.app.subscription.domain.model.BoughtDomainModel
import com.migrely.app.subscription.domain.model.EntitlementDomainModel
import com.migrely.app.subscription.domain.model.EntitlementKindDomainModel
import com.migrely.app.subscription.domain.model.OwnedPurchaseDomainModel
import com.migrely.app.subscription.domain.model.PremiumPlanDomainModel
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.domain.model.PurchaseProductDomainModel
import com.migrely.app.subscription.domain.model.PurchaseStateDomainModel
import java.time.Duration
import java.time.Instant

/** How often Google Play is asked at most, unless forced (BRD D106). */
internal val CHECK_INTERVAL: Duration = Duration.ofMinutes(15)

/** How long a subscription stays on without an answer from Google Play (BRD EC-BIL-01). */
internal val OFFLINE_ALLOWANCE: Duration = Duration.ofDays(3)

/** The yearly plan's free trial (BRD §7.2, D111). */
internal val TRIAL_LENGTH: Duration = Duration.ofDays(7)

/**
 * The rules of the premium entitlement (BRD D106, design D2):
 * - an active subscription or the lifetime purchase unlocks Premium; lifetime wins over both;
 * - a pending purchase never unlocks;
 * - a purchase whose signature does not hold never unlocks;
 * - when Google Play cannot be reached, a subscription stays on for 3 days after the last answer,
 *   and lifetime and everything else stay as they were;
 * - a plan and a trial end are known only for a purchase made on this phone, and are kept while
 *   Google Play reports the same purchase.
 */
class EntitlementRules {
    /** The entitlement Google Play's answer at [now] gives, after [previous]. */
    fun answered(
        previous: EntitlementDomainModel,
        purchases: List<OwnedPurchaseDomainModel>,
        now: Instant,
        bought: BoughtDomainModel? = null
    ): EntitlementDomainModel {
        val trusted = purchases.filter {
            it.signatureValid && it.product != PurchaseProductDomainModel.Other
        }
        val purchased = trusted.filter { it.state == PurchaseStateDomainModel.Purchased }
        val lifetime = purchased.firstOrNull { it.product == PurchaseProductDomainModel.Lifetime }
        val subscription = purchased
            .filter { it.product == PurchaseProductDomainModel.Subscription }
            .maxByOrNull { it.purchaseTime }
        val pending = trusted.firstOrNull { it.state == PurchaseStateDomainModel.Pending }
        return when {
            lifetime != null -> EntitlementDomainModel(
                kind = EntitlementKindDomainModel.Lifetime,
                plan = PremiumPlanDomainModel.Lifetime,
                purchaseToken = lifetime.purchaseToken,
                purchaseTime = lifetime.purchaseTime,
                autoRenewing = false,
                trialEnd = null,
                lastCheck = now,
                subscriptionRenewsToo = subscription?.autoRenewing == true
            )

            subscription != null ->
                known(EntitlementKindDomainModel.Subscription, subscription, previous, bought, now)

            pending != null ->
                known(EntitlementKindDomainModel.Pending, pending, previous, bought, now)

            else -> EntitlementDomainModel.NONE.copy(lastCheck = now)
        }
    }

    /** The entitlement when Google Play did not answer at [now]. */
    fun unanswered(previous: EntitlementDomainModel, now: Instant): EntitlementDomainModel {
        val lastCheck = previous.lastCheck
        val expired = previous.kind == EntitlementKindDomainModel.Subscription &&
            (lastCheck == null || Duration.between(lastCheck, now) > OFFLINE_ALLOWANCE)
        return if (expired) previous.copy(kind = EntitlementKindDomainModel.None) else previous
    }

    /**
     * Whether Google Play is due to be asked again: a pending payment is asked about every time.
     */
    fun isDue(entitlement: EntitlementDomainModel, now: Instant): Boolean {
        val lastCheck = entitlement.lastCheck
        return lastCheck == null ||
            entitlement.kind == EntitlementKindDomainModel.Pending ||
            Duration.between(lastCheck, now) >= CHECK_INTERVAL ||
            now < lastCheck
    }

    fun status(entitlement: EntitlementDomainModel, now: Instant): PremiumStatusDomainModel =
        when (entitlement.kind) {
            EntitlementKindDomainModel.None -> PremiumStatusDomainModel.Free
            EntitlementKindDomainModel.Pending -> PremiumStatusDomainModel.Pending
            EntitlementKindDomainModel.Subscription -> PremiumStatusDomainModel.Subscribed(
                plan = entitlement.plan,
                trialEnd = entitlement.trialEnd?.takeIf { now < it }
            )
            EntitlementKindDomainModel.Lifetime ->
                PremiumStatusDomainModel.Lifetime(entitlement.subscriptionRenewsToo)
        }

    private fun known(
        kind: EntitlementKindDomainModel,
        purchase: OwnedPurchaseDomainModel,
        previous: EntitlementDomainModel,
        bought: BoughtDomainModel?,
        now: Instant
    ): EntitlementDomainModel {
        val boughtHere = bought?.takeIf { it.purchaseToken == purchase.purchaseToken }
        val same = previous.purchaseToken == purchase.purchaseToken
        return EntitlementDomainModel(
            kind = kind,
            plan = boughtHere?.plan ?: previous.plan.takeIf { same },
            purchaseToken = purchase.purchaseToken,
            purchaseTime = purchase.purchaseTime,
            autoRenewing = purchase.autoRenewing,
            trialEnd = if (boughtHere != null) {
                purchase.purchaseTime.plus(TRIAL_LENGTH).takeIf { boughtHere.freeTrial }
            } else {
                previous.trialEnd.takeIf { same }
            },
            lastCheck = now,
            subscriptionRenewsToo = false
        )
    }
}
