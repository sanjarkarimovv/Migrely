package com.migrely.app.subscription.data.repository

import com.migrely.app.datasource.attack.datasource.AttackDataSource
import com.migrely.app.datasource.billing.datasource.BillingDataSource
import com.migrely.app.datasource.billing.model.BillingConnectionDataModel
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.BillingDataException
import com.migrely.app.datasource.exception.BillingUnavailableDataException
import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.preferences.datasource.PreferencesDataSource
import com.migrely.app.datasource.schedule.datasource.PremiumCheckScheduleDataSource
import com.migrely.app.subscription.data.mapper.SubscriptionDataMappers
import com.migrely.app.subscription.data.mapper.SubscriptionProducts
import com.migrely.app.subscription.domain.model.BillingAvailabilityDomainModel
import com.migrely.app.subscription.domain.model.EntitlementDomainModel
import com.migrely.app.subscription.domain.model.OwnedPurchasesResultDomainModel
import com.migrely.app.subscription.domain.model.PaywallMarksDomainModel
import com.migrely.app.subscription.domain.model.PaywallTriggerDomainModel
import com.migrely.app.subscription.domain.model.PremiumProductsResultDomainModel
import com.migrely.app.subscription.domain.model.PurchaseLaunchDomainModel
import com.migrely.app.subscription.domain.model.PurchaseSheetResultDomainModel
import com.migrely.app.subscription.domain.model.SubscriptionWriteResultDomainModel
import com.migrely.app.subscription.domain.repository.AcknowledgePurchaseRepository
import com.migrely.app.subscription.domain.repository.DismissTrialBannerRepository
import com.migrely.app.subscription.domain.repository.GetAttackInProgressRepository
import com.migrely.app.subscription.domain.repository.GetBillingAvailabilityRepository
import com.migrely.app.subscription.domain.repository.GetCurrentTimeRepository
import com.migrely.app.subscription.domain.repository.GetEntitlementRepository
import com.migrely.app.subscription.domain.repository.GetMigraineCountRepository
import com.migrely.app.subscription.domain.repository.GetOwnedPurchasesRepository
import com.migrely.app.subscription.domain.repository.GetPaywallMarksRepository
import com.migrely.app.subscription.domain.repository.GetPremiumProductsRepository
import com.migrely.app.subscription.domain.repository.GetPurchaseUpdatesRepository
import com.migrely.app.subscription.domain.repository.GetTrialBannerDismissalRepository
import com.migrely.app.subscription.domain.repository.LaunchPurchaseRepository
import com.migrely.app.subscription.domain.repository.ManageSubscriptionRepository
import com.migrely.app.subscription.domain.repository.MarkPaywallShownRepository
import com.migrely.app.subscription.domain.repository.SaveEntitlementRepository
import com.migrely.app.subscription.domain.repository.SchedulePremiumChecksRepository
import com.migrely.app.subscription.domain.repository.ShowPaymentMessagesRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow

/**
 * Migrely Premium over Google Play billing, the entitlement kept in the preferences, and the
 * attacks the paywall rules look at (change add-subscription). Every datasource failure comes back
 * as a value (ARCHITECTURE section 7).
 */
@Suppress("TooManyFunctions", "LongParameterList")
class SubscriptionRepository(
    private val billingDataSource: BillingDataSource,
    private val preferencesDataSource: PreferencesDataSource,
    private val attackDataSource: AttackDataSource,
    private val clockDataSource: ClockDataSource,
    private val premiumCheckScheduleDataSource: PremiumCheckScheduleDataSource,
    private val mappers: SubscriptionDataMappers
) : GetEntitlementRepository,
    SaveEntitlementRepository,
    GetOwnedPurchasesRepository,
    AcknowledgePurchaseRepository,
    GetPremiumProductsRepository,
    LaunchPurchaseRepository,
    GetCurrentTimeRepository,
    GetBillingAvailabilityRepository,
    GetAttackInProgressRepository,
    GetMigraineCountRepository,
    GetPaywallMarksRepository,
    MarkPaywallShownRepository,
    GetTrialBannerDismissalRepository,
    DismissTrialBannerRepository,
    ShowPaymentMessagesRepository,
    ManageSubscriptionRepository,
    GetPurchaseUpdatesRepository,
    SchedulePremiumChecksRepository {
    override fun entitlement(): EntitlementDomainModel =
        mappers.entitlementDomainMapper.toDomain(preferencesDataSource.entitlement())

    override fun saveEntitlement(entitlement: EntitlementDomainModel) = write {
        preferencesDataSource.setEntitlement(mappers.entitlementDataMapper.toData(entitlement))
    }

    override fun ownedPurchases(): OwnedPurchasesResultDomainModel = try {
        OwnedPurchasesResultDomainModel.Owned(
            billingDataSource.purchases().map(mappers.ownedPurchaseDomainMapper::toDomain)
        )
    } catch (_: BillingUnavailableDataException) {
        OwnedPurchasesResultDomainModel.Unavailable
    } catch (_: BillingDataException) {
        OwnedPurchasesResultDomainModel.Unreachable
    }

    override fun purchaseUpdates(): Flow<Unit> = billingDataSource.purchaseUpdates()

    override fun acknowledgePurchase(purchaseToken: String): Boolean =
        billingDataSource.acknowledge(purchaseToken)

    override fun premiumProducts(): PremiumProductsResultDomainModel = try {
        val products = mappers.premiumProductsDomainMapper.toDomain(
            billingDataSource.products(
                subscriptionIds = listOf(SubscriptionProducts.PREMIUM),
                oneTimeIds = listOf(SubscriptionProducts.LIFETIME)
            )
        )
        // Products Play does not offer here are as good as no billing (BRD EC-BIL-11).
        if (products.isEmpty()) {
            PremiumProductsResultDomainModel.Unavailable
        } else {
            PremiumProductsResultDomainModel.Loaded(products)
        }
    } catch (_: BillingUnavailableDataException) {
        PremiumProductsResultDomainModel.Unavailable
    } catch (_: BillingDataException) {
        PremiumProductsResultDomainModel.Unreachable
    }

    override fun launchPurchase(launch: PurchaseLaunchDomainModel): PurchaseSheetResultDomainModel =
        mappers.purchaseSheetResultDomainMapper.toDomain(
            billingDataSource.purchase(mappers.purchaseRequestDataMapper.toData(launch))
        )

    override fun now(): Instant = clockDataSource.now()

    override fun schedulePremiumChecks(needed: Boolean) {
        try {
            if (needed) {
                premiumCheckScheduleDataSource.schedule()
            } else {
                premiumCheckScheduleDataSource.cancel()
            }
        } catch (_: DataException) {
            // Planned again at the next check.
        }
    }

    override fun billingAvailability(): BillingAvailabilityDomainModel =
        when (billingDataSource.connect()) {
            BillingConnectionDataModel.Available -> BillingAvailabilityDomainModel.Available
            BillingConnectionDataModel.Unavailable -> BillingAvailabilityDomainModel.Unavailable
            BillingConnectionDataModel.Failed -> BillingAvailabilityDomainModel.Unknown
        }

    override fun attackInProgress(): Boolean? = try {
        attackDataSource.inProgress() != null
    } catch (_: DataException) {
        null
    }

    override fun migraineCount(): Int? = try {
        attackDataSource.count()
    } catch (_: DataException) {
        null
    }

    override fun paywallMarks() = PaywallMarksDomainModel(
        shown = PaywallTriggerDomainModel.entries.filter {
            preferencesDataSource.paywallShown(mappers.paywallTriggerDataMapper.toData(it))
        }.toSet(),
        lastShown = preferencesDataSource.lastPaywallShownMillis()?.let(Instant::ofEpochMilli)
    )

    override fun markPaywallShown(trigger: PaywallTriggerDomainModel, at: Instant) = write {
        preferencesDataSource.markPaywallShown(
            mappers.paywallTriggerDataMapper.toData(trigger),
            at.toEpochMilli()
        )
    }

    override fun dismissedTrialEnd(): Instant? =
        preferencesDataSource.trialBannerDismissedFor()?.let(Instant::ofEpochMilli)

    override fun dismissTrialBanner(trialEnd: Instant) = write {
        preferencesDataSource.dismissTrialBanner(trialEnd.toEpochMilli())
    }

    override fun showPaymentMessages(): Boolean = billingDataSource.showInAppMessages()

    override fun manageSubscription() =
        billingDataSource.manageSubscription(SubscriptionProducts.PREMIUM)

    private inline fun write(block: () -> Unit) = try {
        block()
        SubscriptionWriteResultDomainModel.Done
    } catch (_: DataException) {
        SubscriptionWriteResultDomainModel.Failed
    }
}
