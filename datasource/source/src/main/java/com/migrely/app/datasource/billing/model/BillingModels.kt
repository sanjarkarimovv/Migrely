package com.migrely.app.datasource.billing.model

/**
 * Whether Google Play billing can be used: [Unavailable] where Play has no billing (BRD EC-BIL-11).
 */
enum class BillingConnectionDataModel {
    Available,
    Unavailable,

    /** Google Play did not answer, so availability is unknown. */
    Failed
}

enum class BillingProductKindDataModel {
    Subscription,
    OneTime
}

/**
 * A product with the ways Google Play offers it: a subscription's base plans and offers, or one
 * price.
 */
data class BillingProductDataModel(
    val productId: String,
    val kind: BillingProductKindDataModel,
    val offers: List<BillingOfferDataModel>
)

/**
 * One way to buy a product. [formattedPrice] is Google Play's local price string, for a
 * subscription its recurring price. [billingPeriod] and [freeTrialPeriod] are ISO 8601 periods such
 * as `P1Y` and `P7D`; [freeTrialPeriod] is set when the offer starts with a free phase. A one-time
 * product has no base plan, period or trial.
 */
data class BillingOfferDataModel(
    val basePlanId: String?,
    val offerId: String?,
    val offerToken: String?,
    val formattedPrice: String,
    val billingPeriod: String?,
    val freeTrialPeriod: String?
)

enum class BillingPurchaseStateDataModel {
    Purchased,
    Pending,
    Unspecified
}

/**
 * A purchase as Google Play reports it. Google Play does not say which base plan a subscription is
 * on. [signatureValid] is false when its signature does not match the app's licence key.
 */
data class BillingPurchaseDataModel(
    val productIds: List<String>,
    val state: BillingPurchaseStateDataModel,
    val purchaseTimeMillis: Long,
    val autoRenewing: Boolean,
    val purchaseToken: String,
    val acknowledged: Boolean,
    val signatureValid: Boolean
)

/**
 * What to buy: [offerToken] picks a subscription's base plan or offer. [oldPurchaseToken] is the
 * subscription being replaced, which Google Play prorates by time (BRD EC-BIL-10).
 */
data class BillingPurchaseRequestDataModel(
    val productId: String,
    val offerToken: String?,
    val oldPurchaseToken: String?
)

sealed interface BillingPurchaseResultDataModel {
    /** The sheet closed with a purchase, which may still be pending. */
    data class Completed(val purchases: List<BillingPurchaseDataModel>) :
        BillingPurchaseResultDataModel

    data object Cancelled : BillingPurchaseResultDataModel

    data object AlreadyOwned : BillingPurchaseResultDataModel

    data object Unavailable : BillingPurchaseResultDataModel

    data object Failed : BillingPurchaseResultDataModel
}
