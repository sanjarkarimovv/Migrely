package com.migrely.app.subscription.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.subscription.domain.model.BillingAvailabilityDomainModel
import com.migrely.app.subscription.domain.model.PaywallTriggerDomainModel
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.domain.repository.GetAttackInProgressRepository
import com.migrely.app.subscription.domain.repository.GetBillingAvailabilityRepository
import com.migrely.app.subscription.domain.repository.GetCurrentTimeRepository
import com.migrely.app.subscription.domain.repository.GetEntitlementRepository
import com.migrely.app.subscription.domain.repository.GetMigraineCountRepository
import com.migrely.app.subscription.domain.repository.GetPaywallMarksRepository
import java.time.Duration

/** The paywall after this many migraines: "see your patterns" (BRD §7.3, D110). */
internal const val MIGRAINES_FOR_PATTERNS = 5

/** The least time between two paywalls that show themselves (BRD D110). */
internal val SELF_SHOWN_GAP: Duration = Duration.ofDays(7)

/**
 * Whether a paywall shows itself for the trigger (BRD §7.3, D110, design D5). Only when all hold:
 * - the user is free, so neither premium nor waiting for a payment;
 * - the trigger has not shown one before, and no paywall showed itself in the last 7 days;
 * - for the 5th migraine, at least 5 are logged;
 * - Google Play billing is not known to be unavailable;
 * - no attack is in progress, or it cannot be told. Asked last, after Google Play, which may take
 *   a moment, so an attack started meanwhile is seen.
 */
class ShouldShowPaywallUseCase(
    private val getEntitlementRepository: GetEntitlementRepository,
    private val getPaywallMarksRepository: GetPaywallMarksRepository,
    private val getAttackInProgressRepository: GetAttackInProgressRepository,
    private val getMigraineCountRepository: GetMigraineCountRepository,
    private val getBillingAvailabilityRepository: GetBillingAvailabilityRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val entitlementRules: EntitlementRules,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<PaywallTriggerDomainModel, Boolean>(coroutineContextProvider) {
    override fun executeInBackground(request: PaywallTriggerDomainModel): Boolean {
        val now = getCurrentTimeRepository.now()
        val status = entitlementRules.status(getEntitlementRepository.entitlement(), now)
        val marks = getPaywallMarksRepository.paywallMarks()
        val lastShown = marks.lastShown
        return status == PremiumStatusDomainModel.Free &&
            request !in marks.shown &&
            (lastShown == null || Duration.between(lastShown, now) >= SELF_SHOWN_GAP) &&
            (
                request != PaywallTriggerDomainModel.FifthMigraine ||
                    (getMigraineCountRepository.migraineCount() ?: 0) >= MIGRAINES_FOR_PATTERNS
                ) &&
            getBillingAvailabilityRepository.billingAvailability() !=
            BillingAvailabilityDomainModel.Unavailable &&
            getAttackInProgressRepository.attackInProgress() == false
    }
}
