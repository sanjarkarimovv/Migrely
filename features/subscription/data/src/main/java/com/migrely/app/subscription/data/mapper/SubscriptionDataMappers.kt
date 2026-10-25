package com.migrely.app.subscription.data.mapper

/** The mappers the subscription repository uses, grouped. */
class SubscriptionDataMappers(
    val entitlementDomainMapper: EntitlementDomainMapper,
    val entitlementDataMapper: EntitlementDataMapper,
    val ownedPurchaseDomainMapper: OwnedPurchaseDomainMapper,
    val premiumProductsDomainMapper: PremiumProductsDomainMapper,
    val purchaseRequestDataMapper: PurchaseRequestDataMapper,
    val purchaseSheetResultDomainMapper: PurchaseSheetResultDomainMapper,
    val paywallTriggerDataMapper: PaywallTriggerDataMapper,
    val launchedPurchaseDomainMapper: LaunchedPurchaseDomainMapper,
    val launchedPurchaseDataMapper: LaunchedPurchaseDataMapper
) {
    companion object {
        fun create(): SubscriptionDataMappers {
            val ownedPurchaseDomainMapper = OwnedPurchaseDomainMapper()
            return SubscriptionDataMappers(
                entitlementDomainMapper = EntitlementDomainMapper(),
                entitlementDataMapper = EntitlementDataMapper(),
                ownedPurchaseDomainMapper = ownedPurchaseDomainMapper,
                premiumProductsDomainMapper = PremiumProductsDomainMapper(),
                purchaseRequestDataMapper = PurchaseRequestDataMapper(),
                purchaseSheetResultDomainMapper =
                PurchaseSheetResultDomainMapper(ownedPurchaseDomainMapper),
                paywallTriggerDataMapper = PaywallTriggerDataMapper(),
                launchedPurchaseDomainMapper = LaunchedPurchaseDomainMapper(),
                launchedPurchaseDataMapper = LaunchedPurchaseDataMapper()
            )
        }
    }
}
