package com.migrely.app.subscription.data.mapper

import com.migrely.app.datasource.preferences.model.LaunchedPurchaseDataModel
import com.migrely.app.subscription.domain.model.LaunchedPurchaseDomainModel
import com.migrely.app.subscription.domain.model.PremiumPlanDomainModel
import java.time.Instant

/** The launched purchase as kept; one this version cannot read is none. */
class LaunchedPurchaseDomainMapper {
    fun toDomain(launched: LaunchedPurchaseDataModel?): LaunchedPurchaseDomainModel? {
        launched ?: return null
        val plan = when {
            launched.productId == SubscriptionProducts.LIFETIME -> PremiumPlanDomainModel.Lifetime
            launched.productId != SubscriptionProducts.PREMIUM -> null
            launched.basePlanId == SubscriptionProducts.YEARLY -> PremiumPlanDomainModel.Yearly
            launched.basePlanId == SubscriptionProducts.MONTHLY -> PremiumPlanDomainModel.Monthly
            else -> null
        } ?: return null
        return LaunchedPurchaseDomainModel(
            plan = plan,
            freeTrial = trialPeriodOf(launched.freeTrialPeriod),
            at = Instant.ofEpochMilli(launched.launchedAtMillis)
        )
    }
}

class LaunchedPurchaseDataMapper {
    fun toData(launched: LaunchedPurchaseDomainModel) = LaunchedPurchaseDataModel(
        productId = when (launched.plan) {
            PremiumPlanDomainModel.Yearly, PremiumPlanDomainModel.Monthly ->
                SubscriptionProducts.PREMIUM
            PremiumPlanDomainModel.Lifetime -> SubscriptionProducts.LIFETIME
        },
        basePlanId = when (launched.plan) {
            PremiumPlanDomainModel.Yearly -> SubscriptionProducts.YEARLY
            PremiumPlanDomainModel.Monthly -> SubscriptionProducts.MONTHLY
            PremiumPlanDomainModel.Lifetime -> null
        },
        freeTrialPeriod = launched.freeTrial?.toString(),
        launchedAtMillis = launched.at.toEpochMilli()
    )
}
