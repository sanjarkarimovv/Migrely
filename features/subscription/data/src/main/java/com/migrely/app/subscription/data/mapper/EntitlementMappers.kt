package com.migrely.app.subscription.data.mapper

import com.migrely.app.datasource.preferences.model.EntitlementDataModel
import com.migrely.app.datasource.preferences.model.EntitlementKindDataModel
import com.migrely.app.subscription.domain.model.EntitlementDomainModel
import com.migrely.app.subscription.domain.model.EntitlementKindDomainModel
import com.migrely.app.subscription.domain.model.PremiumPlanDomainModel
import java.time.Instant

/** The stored entitlement; nothing stored is [EntitlementDomainModel.NONE]. */
class EntitlementDomainMapper {
    fun toDomain(entitlement: EntitlementDataModel?): EntitlementDomainModel {
        entitlement ?: return EntitlementDomainModel.NONE
        return EntitlementDomainModel(
            kind = when (entitlement.kind) {
                EntitlementKindDataModel.None -> EntitlementKindDomainModel.None
                EntitlementKindDataModel.Subscription -> EntitlementKindDomainModel.Subscription
                EntitlementKindDataModel.Lifetime -> EntitlementKindDomainModel.Lifetime
                EntitlementKindDataModel.Pending -> EntitlementKindDomainModel.Pending
            },
            plan = planOf(entitlement.productId, entitlement.basePlanId),
            purchaseToken = entitlement.purchaseToken,
            purchaseTime = entitlement.purchaseTimeMillis?.let(Instant::ofEpochMilli),
            autoRenewing = entitlement.autoRenewing,
            trialEnd = entitlement.trialEndMillis?.let(Instant::ofEpochMilli),
            lastCheck = entitlement.lastCheckMillis?.let(Instant::ofEpochMilli),
            subscriptionRenewsToo = entitlement.subscriptionRenewsToo
        )
    }

    private fun planOf(productId: String?, basePlanId: String?): PremiumPlanDomainModel? = when {
        productId == SubscriptionProducts.LIFETIME -> PremiumPlanDomainModel.Lifetime
        productId != SubscriptionProducts.PREMIUM -> null
        basePlanId == SubscriptionProducts.YEARLY -> PremiumPlanDomainModel.Yearly
        basePlanId == SubscriptionProducts.MONTHLY -> PremiumPlanDomainModel.Monthly
        else -> null
    }
}

class EntitlementDataMapper {
    fun toData(entitlement: EntitlementDomainModel) = EntitlementDataModel(
        kind = when (entitlement.kind) {
            EntitlementKindDomainModel.None -> EntitlementKindDataModel.None
            EntitlementKindDomainModel.Subscription -> EntitlementKindDataModel.Subscription
            EntitlementKindDomainModel.Lifetime -> EntitlementKindDataModel.Lifetime
            EntitlementKindDomainModel.Pending -> EntitlementKindDataModel.Pending
        },
        productId = when (entitlement.plan) {
            PremiumPlanDomainModel.Yearly, PremiumPlanDomainModel.Monthly ->
                SubscriptionProducts.PREMIUM
            PremiumPlanDomainModel.Lifetime -> SubscriptionProducts.LIFETIME
            null -> SubscriptionProducts.PREMIUM.takeIf {
                entitlement.kind == EntitlementKindDomainModel.Subscription
            }
        },
        basePlanId = when (entitlement.plan) {
            PremiumPlanDomainModel.Yearly -> SubscriptionProducts.YEARLY
            PremiumPlanDomainModel.Monthly -> SubscriptionProducts.MONTHLY
            PremiumPlanDomainModel.Lifetime, null -> null
        },
        purchaseToken = entitlement.purchaseToken,
        purchaseTimeMillis = entitlement.purchaseTime?.toEpochMilli(),
        autoRenewing = entitlement.autoRenewing,
        trialEndMillis = entitlement.trialEnd?.toEpochMilli(),
        lastCheckMillis = entitlement.lastCheck?.toEpochMilli(),
        subscriptionRenewsToo = entitlement.subscriptionRenewsToo
    )
}
