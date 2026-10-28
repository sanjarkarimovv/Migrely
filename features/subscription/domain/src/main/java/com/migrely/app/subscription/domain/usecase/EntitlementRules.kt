package com.migrely.app.subscription.domain.usecase

import com.migrely.app.subscription.domain.model.BoughtDomainModel
import com.migrely.app.subscription.domain.model.EntitlementDomainModel
import com.migrely.app.subscription.domain.model.EntitlementKindDomainModel
import com.migrely.app.subscription.domain.model.LaunchedPurchaseDomainModel
import com.migrely.app.subscription.domain.model.OwnedPurchaseDomainModel
import com.migrely.app.subscription.domain.model.PremiumPlanDomainModel
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.domain.model.PurchaseProductDomainModel
import com.migrely.app.subscription.domain.model.PurchaseStateDomainModel
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

/** How often Google Play is asked at most, unless forced (BRD D106). */
internal val CHECK_INTERVAL: Duration = Duration.ofMinutes(15)

/** How long a subscription stays on without an answer from Google Play (BRD EC-BIL-01). */
internal val OFFLINE_ALLOWANCE: Duration = Duration.ofDays(3)

/** How long a launched purchase may wait for the check that gives it its purchase. */
internal val LAUNCH_KEPT: Duration = Duration.ofDays(1)

/** Google Play's purchase time may run slightly ahead of the phone's clock. */
private val LAUNCH_CLOCK_MARGIN: Duration = Duration.ofMinutes(5)

/**
 * The rules of the premium entitlement (BRD D106, design D2):
 * - an active subscription or the lifetime purchase unlocks Premium; lifetime wins over both;
 * - a purchase that unlocks but is not yet acknowledged is marked, so it is acknowledged before
 *   Google Play's 3 days are up;
 * - a pending purchase never unlocks;
 * - a purchase whose signature does not hold never unlocks;
 * - when Google Play cannot be reached, a subscription stays on for 3 days after the last answer,
 *   but not when the clock now reads earlier than that answer, and lifetime and everything else
 *   stay as they were;
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
        val unacknowledged = purchased.any { !it.acknowledged }
        val entitlement = when {
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
        return entitlement.copy(acknowledgementPending = unacknowledged)
    }

    /**
     * The purchase that a sheet launched at [LaunchedPurchaseDomainModel.at] became: a new one of
     * its product, made since then, while the launch is less than a day old (review M5).
     */
    fun boughtFrom(
        launched: LaunchedPurchaseDomainModel,
        purchases: List<OwnedPurchaseDomainModel>,
        previous: EntitlementDomainModel,
        now: Instant
    ): BoughtDomainModel? {
        if (Duration.between(launched.at, now) > LAUNCH_KEPT) return null
        val product = productOf(launched.plan)
        return purchases.firstOrNull {
            it.product == product &&
                it.purchaseToken != previous.purchaseToken &&
                !it.purchaseTime.isBefore(launched.at.minus(LAUNCH_CLOCK_MARGIN))
        }?.let { BoughtDomainModel(it.purchaseToken, launched.plan, launched.freeTrial) }
    }

    fun productOf(plan: PremiumPlanDomainModel): PurchaseProductDomainModel = when (plan) {
        PremiumPlanDomainModel.Yearly,
        PremiumPlanDomainModel.Monthly -> PurchaseProductDomainModel.Subscription
        PremiumPlanDomainModel.Lifetime -> PurchaseProductDomainModel.Lifetime
    }

    /** Whether background checks are needed: a payment is pending, or one is not acknowledged. */
    fun needsBackgroundChecks(entitlement: EntitlementDomainModel): Boolean =
        entitlement.kind == EntitlementKindDomainModel.Pending || entitlement.acknowledgementPending

    /** The entitlement when Google Play did not answer at [now]. */
    fun unanswered(previous: EntitlementDomainModel, now: Instant): EntitlementDomainModel {
        val lastCheck = previous.lastCheck
        // A clock set back before the last answer cannot stretch the allowance (review M6).
        val expired = previous.kind == EntitlementKindDomainModel.Subscription &&
            (
                lastCheck == null ||
                    now < lastCheck ||
                    Duration.between(lastCheck, now) > OFFLINE_ALLOWANCE
                )
        return if (expired) previous.copy(kind = EntitlementKindDomainModel.None) else previous
    }

    /**
     * Whether Google Play is due to be asked again: a pending payment, and a purchase not yet
     * acknowledged, are asked about every time, and so after a clock that went back before the
     * last answer, and after a purchase sheet [launched] since that answer whose own answer never
     * came back, as when the app was closed with the sheet open (EC-BIL-13).
     */
    fun isDue(
        entitlement: EntitlementDomainModel,
        now: Instant,
        launched: LaunchedPurchaseDomainModel?
    ): Boolean {
        val lastCheck = entitlement.lastCheck
        return lastCheck == null ||
            needsBackgroundChecks(entitlement) ||
            now < lastCheck ||
            Duration.between(lastCheck, now) >= CHECK_INTERVAL ||
            (launched != null && neverAnswered(launched, lastCheck, now))
    }

    /**
     * A launch at or after the last answer, not ahead of the clock, and young enough to be given
     * its purchase: a clock set back, or a launch from days ago, does not ask Play at every turn.
     */
    private fun neverAnswered(
        launched: LaunchedPurchaseDomainModel,
        lastCheck: Instant,
        now: Instant
    ) = !launched.at.isBefore(lastCheck) &&
        !now.isBefore(launched.at) &&
        Duration.between(launched.at, now) <= LAUNCH_KEPT

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
                boughtHere.freeTrial?.let { trial ->
                    purchase.purchaseTime.atOffset(ZoneOffset.UTC).plus(trial).toInstant()
                }
            } else {
                previous.trialEnd.takeIf { same }
            },
            lastCheck = now,
            subscriptionRenewsToo = false
        )
    }
}
