package com.migrely.app.subscription.domain.model

import java.time.Instant

/** The three ways to buy Migrely Premium (BRD §7.2, D107). */
enum class PremiumPlanDomainModel {
    Yearly,
    Monthly,
    Lifetime
}

/** What the user has now (BRD D106, D112). */
sealed interface PremiumStatusDomainModel {
    data object Free : PremiumStatusDomainModel

    /** A payment that completes later; it unlocks nothing yet (BRD EC-BIL-08). */
    data object Pending : PremiumStatusDomainModel

    /**
     * An active subscription. [plan] is null when it was bought on another phone, since Google Play
     * does not say. [trialEnd] is set while a free trial started on this phone is running.
     */
    data class Subscribed(val plan: PremiumPlanDomainModel?, val trialEnd: Instant?) :
        PremiumStatusDomainModel

    /** [subscriptionRenewsToo] when a subscription still renews and must be cancelled in Play. */
    data class Lifetime(val subscriptionRenewsToo: Boolean) : PremiumStatusDomainModel
}

enum class EntitlementKindDomainModel {
    None,
    Subscription,
    Lifetime,
    Pending
}

/**
 * The entitlement as the last check decided it (BRD D106). [plan] and [trialEnd] are known only for
 * a purchase made on this phone; [lastCheck] is the last time Google Play answered.
 * [acknowledgementPending] is true while a purchase that unlocks is not yet acknowledged: Google
 * Play is then asked at every chance, and in the background, until it is (review I2).
 */
data class EntitlementDomainModel(
    val kind: EntitlementKindDomainModel,
    val plan: PremiumPlanDomainModel?,
    val purchaseToken: String?,
    val purchaseTime: Instant?,
    val autoRenewing: Boolean,
    val trialEnd: Instant?,
    val lastCheck: Instant?,
    val subscriptionRenewsToo: Boolean,
    val acknowledgementPending: Boolean = false
) {
    companion object {
        /** Before the first check, or after "Delete all data" (BRD D112). */
        val NONE = EntitlementDomainModel(
            kind = EntitlementKindDomainModel.None,
            plan = null,
            purchaseToken = null,
            purchaseTime = null,
            autoRenewing = false,
            trialEnd = null,
            lastCheck = null,
            subscriptionRenewsToo = false
        )
    }
}

/** Which of Migrely's products a purchase is for. */
enum class PurchaseProductDomainModel {
    Subscription,
    Lifetime,
    Other
}

enum class PurchaseStateDomainModel {
    Purchased,
    Pending,
    Other
}

/** A purchase Google Play reports for this account. */
data class OwnedPurchaseDomainModel(
    val product: PurchaseProductDomainModel,
    val state: PurchaseStateDomainModel,
    val purchaseTime: Instant,
    val autoRenewing: Boolean,
    val purchaseToken: String,
    val acknowledged: Boolean,
    val signatureValid: Boolean
)

sealed interface OwnedPurchasesResultDomainModel {
    data class Owned(val purchases: List<OwnedPurchaseDomainModel>) :
        OwnedPurchasesResultDomainModel

    /** Google Play has no billing here (BRD EC-BIL-11). */
    data object Unavailable : OwnedPurchasesResultDomainModel

    /** Google Play did not answer, for example offline (BRD EC-BIL-01). */
    data object Unreachable : OwnedPurchasesResultDomainModel
}

/** Whether Google Play billing can be used, as far as is known. */
enum class BillingAvailabilityDomainModel {
    Available,
    Unavailable,
    Unknown
}

/** A product with Google Play's local price (BRD SUB-F1). [freeTrial] when it starts with one. */
data class PremiumProductDomainModel(
    val plan: PremiumPlanDomainModel,
    val price: String,
    val freeTrial: Boolean,
    val offerToken: String?
)

sealed interface PremiumProductsResultDomainModel {
    data class Loaded(val products: List<PremiumProductDomainModel>) :
        PremiumProductsResultDomainModel

    data object Unavailable : PremiumProductsResultDomainModel

    data object Unreachable : PremiumProductsResultDomainModel
}

/**
 * A product as the paywall offers it: [current] is the plan the user has, and [choosable] is false
 * for it and for a move to a cheaper subscription, which is made in Google Play.
 */
data class PaywallOfferDomainModel(
    val product: PremiumProductDomainModel,
    val current: Boolean,
    val choosable: Boolean
)

sealed interface PaywallOffersDomainModel {
    data class Loaded(val offers: List<PaywallOfferDomainModel>) : PaywallOffersDomainModel

    data object Unavailable : PaywallOffersDomainModel

    data object Unreachable : PaywallOffersDomainModel
}

data class PaywallDomainModel(
    val status: PremiumStatusDomainModel,
    val offers: PaywallOffersDomainModel
)

/** What to buy; [replacedPurchaseToken] is the subscription an upgrade replaces (BRD EC-BIL-10). */
data class PurchaseLaunchDomainModel(
    val product: PremiumProductDomainModel,
    val replacedPurchaseToken: String?
)

/** How Google Play's purchase sheet closed. */
sealed interface PurchaseSheetResultDomainModel {
    data class Completed(val purchases: List<OwnedPurchaseDomainModel>) :
        PurchaseSheetResultDomainModel

    data object Cancelled : PurchaseSheetResultDomainModel

    data object AlreadyOwned : PurchaseSheetResultDomainModel

    data object Unavailable : PurchaseSheetResultDomainModel

    data object Failed : PurchaseSheetResultDomainModel

    /** The sheet gave no answer for a long while; a later update or check tells what happened. */
    data object Unanswered : PurchaseSheetResultDomainModel
}

/** A purchase just made on this phone, which is the only time its plan and trial are known. */
data class BoughtDomainModel(
    val purchaseToken: String,
    val plan: PremiumPlanDomainModel,
    val freeTrial: Boolean
)

enum class PurchaseOutcomeDomainModel {
    Unlocked,
    Pending,
    Cancelled,
    Unavailable,
    Failed
}

/** How buying went, and the status after it. */
data class BuyPremiumResultDomainModel(
    val outcome: PurchaseOutcomeDomainModel,
    val status: PremiumStatusDomainModel
)

/** The status after asking Google Play, and whether it answered. */
data class PremiumCheckDomainModel(
    val status: PremiumStatusDomainModel,
    val billing: BillingAvailabilityDomainModel
)

data class RefreshPremiumRequestDomainModel(val force: Boolean)

/** What shows a paywall without a tap (BRD D110). */
enum class PaywallTriggerDomainModel {
    Onboarding,
    FifthMigraine
}

data class PaywallMarksDomainModel(
    val shown: Set<PaywallTriggerDomainModel>,
    val lastShown: Instant?
)

sealed interface TrialReminderDomainModel {
    data object None : TrialReminderDomainModel

    /** The trial ends at [trialEnd], within a day (BRD D111). */
    data class EndsSoon(val trialEnd: Instant) : TrialReminderDomainModel
}

enum class SubscriptionWriteResultDomainModel {
    Done,
    Failed
}
