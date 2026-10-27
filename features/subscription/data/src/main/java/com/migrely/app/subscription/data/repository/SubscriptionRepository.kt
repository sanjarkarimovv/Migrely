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
import com.migrely.app.subscription.domain.model.LaunchedPurchaseDomainModel
import com.migrely.app.subscription.domain.model.MigraineStartDomainModel
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
import com.migrely.app.subscription.domain.repository.GetLaunchedPurchaseRepository
import com.migrely.app.subscription.domain.repository.GetMigraineStartsRepository
import com.migrely.app.subscription.domain.repository.GetOwnedPurchasesRepository
import com.migrely.app.subscription.domain.repository.GetPaywallMarksRepository
import com.migrely.app.subscription.domain.repository.GetPremiumProductsRepository
import com.migrely.app.subscription.domain.repository.GetPurchaseUpdatesRepository
import com.migrely.app.subscription.domain.repository.GetTimeZoneRepository
import com.migrely.app.subscription.domain.repository.GetTrialBannerDismissalRepository
import com.migrely.app.subscription.domain.repository.LaunchPurchaseRepository
import com.migrely.app.subscription.domain.repository.ManageSubscriptionRepository
import com.migrely.app.subscription.domain.repository.MarkPaywallShownRepository
import com.migrely.app.subscription.domain.repository.SaveEntitlementRepository
import com.migrely.app.subscription.domain.repository.SaveLaunchedPurchaseRepository
import com.migrely.app.subscription.domain.repository.SchedulePremiumChecksRepository
import com.migrely.app.subscription.domain.repository.ShowPaymentMessagesRepository
import java.time.DateTimeException
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow

/**
 * How long Google Play's word on whether Premium can be sold here is kept for the paywalls that
 * show themselves, so a home screen resumed again and again asks Play at most once an hour
 * (BRD D110).
 */
internal val PREMIUM_OFFER_MEMORY: Duration = Duration.ofHours(1)

/**
 * Migrely Premium over Google Play billing, the entitlement kept in the preferences, and the
 * attacks the paywall rules look at (change add-subscription). Every datasource failure comes back
 * as a value (ARCHITECTURE section 7). One for the app, so what Play said about the products is
 * remembered across screens.
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
    GetMigraineStartsRepository,
    GetTimeZoneRepository,
    GetPaywallMarksRepository,
    MarkPaywallShownRepository,
    GetTrialBannerDismissalRepository,
    DismissTrialBannerRepository,
    ShowPaymentMessagesRepository,
    ManageSubscriptionRepository,
    GetPurchaseUpdatesRepository,
    SchedulePremiumChecksRepository,
    GetLaunchedPurchaseRepository,
    SaveLaunchedPurchaseRepository {
    override fun entitlement(): EntitlementDomainModel =
        mappers.entitlementDomainMapper.toDomain(preferencesDataSource.entitlement())

    override fun saveEntitlement(entitlement: EntitlementDomainModel) = write {
        preferencesDataSource.setEntitlement(mappers.entitlementDataMapper.toData(entitlement))
    }

    override fun launchedPurchase(): LaunchedPurchaseDomainModel? =
        mappers.launchedPurchaseDomainMapper.toDomain(preferencesDataSource.launchedPurchase())

    // A launch that cannot be kept only loses its plan and trial if the app is closed meanwhile.
    override fun saveLaunchedPurchase(launched: LaunchedPurchaseDomainModel?) {
        write {
            preferencesDataSource.setLaunchedPurchase(
                launched?.let(mappers.launchedPurchaseDataMapper::toData)
            )
        }
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

    @Volatile
    private var rememberedAvailability: RememberedAvailability? = null

    /** Google Play's answer, read afresh: it also renews what [billingAvailability] remembers. */
    override fun premiumProducts(): PremiumProductsResultDomainModel = queryPremiumProducts().also {
        remember(availabilityOf(it))
    }

    private fun queryPremiumProducts(): PremiumProductsResultDomainModel = try {
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

    /**
     * Available only when billing connects and Google Play offers Migrely's products: products
     * Play does not return, as before they are set up in the Play Console, are as good as no
     * billing for a paywall that shows itself (BRD D110, EC-BIL-11). A known answer is kept for
     * [PREMIUM_OFFER_MEMORY]; one Play did not give is asked for again next time.
     */
    override fun billingAvailability(): BillingAvailabilityDomainModel {
        val now = clockDataSource.now()
        rememberedAvailability?.takeIf { it.isFreshAt(now) }?.let { return it.availability }
        return when (billingDataSource.connect()) {
            BillingConnectionDataModel.Available -> availabilityOf(premiumProducts())
            BillingConnectionDataModel.Unavailable ->
                BillingAvailabilityDomainModel.Unavailable.also(::remember)

            BillingConnectionDataModel.Failed ->
                BillingAvailabilityDomainModel.Unknown.also(::remember)
        }
    }

    private fun availabilityOf(products: PremiumProductsResultDomainModel) = when (products) {
        is PremiumProductsResultDomainModel.Loaded -> BillingAvailabilityDomainModel.Available
        PremiumProductsResultDomainModel.Unavailable -> BillingAvailabilityDomainModel.Unavailable
        PremiumProductsResultDomainModel.Unreachable -> BillingAvailabilityDomainModel.Unknown
    }

    private fun remember(availability: BillingAvailabilityDomainModel) {
        rememberedAvailability = when (availability) {
            BillingAvailabilityDomainModel.Available, BillingAvailabilityDomainModel.Unavailable ->
                RememberedAvailability(availability, clockDataSource.now())

            BillingAvailabilityDomainModel.Unknown -> null
        }
    }

    override fun attackInProgress(): Boolean? = try {
        attackDataSource.inProgress() != null
    } catch (_: DataException) {
        null
    }

    /** Every attack not deleted: those touching any time up to now, the one going on too. */
    override fun migraineStarts(): List<MigraineStartDomainModel>? = try {
        attackDataSource.overlapping(null, Instant.EPOCH, clockDataSource.now()).mapNotNull {
            try {
                MigraineStartDomainModel(it.start, ZoneId.of(it.startZoneId))
            } catch (_: DateTimeException) {
                null
            }
        }
    } catch (_: DataException) {
        null
    }

    override fun zone(): ZoneId = clockDataSource.zone()

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

/** What Google Play said about selling Premium here, and when. */
private class RememberedAvailability(
    val availability: BillingAvailabilityDomainModel,
    private val at: Instant
) {
    /** Fresh for a while; a clock set back makes it stale at once. */
    fun isFreshAt(now: Instant) =
        !now.isBefore(at) && Duration.between(at, now) < PREMIUM_OFFER_MEMORY
}
