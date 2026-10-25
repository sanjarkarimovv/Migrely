package com.migrely.app.subscription.domain.usecase

import com.migrely.app.subscription.domain.model.EntitlementDomainModel
import com.migrely.app.subscription.domain.model.EntitlementKindDomainModel
import com.migrely.app.subscription.domain.model.OwnedPurchaseDomainModel
import com.migrely.app.subscription.domain.model.PremiumPlanDomainModel
import com.migrely.app.subscription.domain.model.PremiumProductDomainModel
import com.migrely.app.subscription.domain.model.PurchaseProductDomainModel
import com.migrely.app.subscription.domain.model.PurchaseStateDomainModel
import java.time.Duration
import java.time.Instant

internal val NOW: Instant = Instant.parse("2026-10-25T09:00:00Z")
internal val BOUGHT_AT: Instant = Instant.parse("2026-10-20T09:00:00Z")

internal val YEARLY_TRIAL = PremiumProductDomainModel(
    PremiumPlanDomainModel.Yearly,
    "€29.99",
    freeTrial = true,
    offerToken = "t-trial"
)
internal val MONTHLY =
    PremiumProductDomainModel(PremiumPlanDomainModel.Monthly, "€4.99", false, "t-monthly")
internal val LIFETIME =
    PremiumProductDomainModel(PremiumPlanDomainModel.Lifetime, "€59.99", false, "t-lifetime")

internal fun purchase(
    product: PurchaseProductDomainModel = PurchaseProductDomainModel.Subscription,
    state: PurchaseStateDomainModel = PurchaseStateDomainModel.Purchased,
    token: String = "sub",
    at: Instant = BOUGHT_AT,
    autoRenewing: Boolean = true,
    acknowledged: Boolean = true,
    signatureValid: Boolean = true
) = OwnedPurchaseDomainModel(product, state, at, autoRenewing, token, acknowledged, signatureValid)

internal fun subscription(
    plan: PremiumPlanDomainModel? = PremiumPlanDomainModel.Monthly,
    token: String = "sub",
    lastCheck: Instant? = NOW.minus(Duration.ofHours(1)),
    trialEnd: Instant? = null
) = EntitlementDomainModel(
    kind = EntitlementKindDomainModel.Subscription,
    plan = plan,
    purchaseToken = token,
    purchaseTime = BOUGHT_AT,
    autoRenewing = true,
    trialEnd = trialEnd,
    lastCheck = lastCheck,
    subscriptionRenewsToo = false
)
