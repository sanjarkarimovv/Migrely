package com.migrely.app.datasource.preferences.model

/**
 * The premium entitlement as the last check decided it (BRD D106). Times are epoch milliseconds.
 * [basePlanId] and [trialEndMillis] are known only when the purchase was made on this phone, since
 * Google Play does not report them. [lastCheckMillis] is the last time Google Play answered.
 * [subscriptionRenewsToo] is true when lifetime was bought while a subscription still renews
 * (BRD EC-BIL-09). [acknowledgementPending] is true while a purchase that unlocks is not yet
 * acknowledged (review I2).
 */
data class EntitlementDataModel(
    val kind: EntitlementKindDataModel,
    val productId: String?,
    val basePlanId: String?,
    val purchaseToken: String?,
    val purchaseTimeMillis: Long?,
    val autoRenewing: Boolean,
    val trialEndMillis: Long?,
    val lastCheckMillis: Long?,
    val subscriptionRenewsToo: Boolean,
    val acknowledgementPending: Boolean = false
)

/** [key] is what is stored and must never change. */
enum class EntitlementKindDataModel(val key: String) {
    None("none"),
    Subscription("subscription"),
    Lifetime("lifetime"),
    Pending("pending")
}
