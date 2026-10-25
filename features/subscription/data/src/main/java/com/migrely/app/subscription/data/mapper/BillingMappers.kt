package com.migrely.app.subscription.data.mapper

import com.migrely.app.datasource.billing.model.BillingOfferDataModel
import com.migrely.app.datasource.billing.model.BillingProductDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseRequestDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseResultDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseStateDataModel
import com.migrely.app.datasource.preferences.model.PaywallTriggerDataModel
import com.migrely.app.subscription.domain.model.OwnedPurchaseDomainModel
import com.migrely.app.subscription.domain.model.PaywallTriggerDomainModel
import com.migrely.app.subscription.domain.model.PremiumPlanDomainModel
import com.migrely.app.subscription.domain.model.PremiumProductDomainModel
import com.migrely.app.subscription.domain.model.PurchaseLaunchDomainModel
import com.migrely.app.subscription.domain.model.PurchaseProductDomainModel
import com.migrely.app.subscription.domain.model.PurchaseSheetResultDomainModel
import com.migrely.app.subscription.domain.model.PurchaseStateDomainModel
import java.time.Instant
import java.time.Period
import java.time.format.DateTimeParseException

class OwnedPurchaseDomainMapper {
    fun toDomain(purchase: BillingPurchaseDataModel) = OwnedPurchaseDomainModel(
        product = when {
            SubscriptionProducts.LIFETIME in purchase.productIds ->
                PurchaseProductDomainModel.Lifetime
            SubscriptionProducts.PREMIUM in purchase.productIds ->
                PurchaseProductDomainModel.Subscription
            else -> PurchaseProductDomainModel.Other
        },
        state = when (purchase.state) {
            BillingPurchaseStateDataModel.Purchased -> PurchaseStateDomainModel.Purchased
            BillingPurchaseStateDataModel.Pending -> PurchaseStateDomainModel.Pending
            BillingPurchaseStateDataModel.Unspecified -> PurchaseStateDomainModel.Other
        },
        purchaseTime = Instant.ofEpochMilli(purchase.purchaseTimeMillis),
        autoRenewing = purchase.autoRenewing,
        purchaseToken = purchase.purchaseToken,
        acknowledged = purchase.acknowledged,
        signatureValid = purchase.signatureValid
    )
}

/**
 * Google Play's products as the paywall offers them, yearly, monthly, then lifetime: the yearly
 * plan with its free trial when Play offers it to this account, else its base plan (BRD D107,
 * D109).
 */
class PremiumProductsDomainMapper {
    fun toDomain(products: List<BillingProductDataModel>): List<PremiumProductDomainModel> {
        val premium = products.firstOrNull { it.productId == SubscriptionProducts.PREMIUM }
            ?.offers.orEmpty()
        val yearly = premium.filter { it.basePlanId == SubscriptionProducts.YEARLY }
        val yearlyOffer = yearly.firstOrNull {
            it.offerId == SubscriptionProducts.TRIAL_OFFER && it.freeTrialPeriod != null
        } ?: yearly.firstOrNull { it.offerId == null }
        val monthlyOffer = premium.firstOrNull {
            it.basePlanId == SubscriptionProducts.MONTHLY && it.offerId == null
        }
        val lifetimeOffer = products.firstOrNull { it.productId == SubscriptionProducts.LIFETIME }
            ?.offers?.firstOrNull()
        return listOfNotNull(
            yearlyOffer?.let { product(PremiumPlanDomainModel.Yearly, it) },
            monthlyOffer?.let { product(PremiumPlanDomainModel.Monthly, it) },
            lifetimeOffer?.let { product(PremiumPlanDomainModel.Lifetime, it) }
        )
    }

    private fun product(plan: PremiumPlanDomainModel, offer: BillingOfferDataModel) =
        PremiumProductDomainModel(
            plan = plan,
            price = offer.formattedPrice,
            freeTrial = trialPeriodOf(offer.freeTrialPeriod),
            offerToken = offer.offerToken
        )
}

/** Google Play's ISO 8601 trial period, such as `P7D` or `P1W`; one it cannot read is no trial. */
internal fun trialPeriodOf(period: String?): Period? = try {
    period?.let(Period::parse)?.takeUnless { it.isZero || it.isNegative }
} catch (_: DateTimeParseException) {
    null
}

class PurchaseRequestDataMapper {
    fun toData(launch: PurchaseLaunchDomainModel) = BillingPurchaseRequestDataModel(
        productId = when (launch.product.plan) {
            PremiumPlanDomainModel.Yearly, PremiumPlanDomainModel.Monthly ->
                SubscriptionProducts.PREMIUM
            PremiumPlanDomainModel.Lifetime -> SubscriptionProducts.LIFETIME
        },
        offerToken = launch.product.offerToken,
        oldPurchaseToken = launch.replacedPurchaseToken
    )
}

class PurchaseSheetResultDomainMapper(
    private val ownedPurchaseDomainMapper: OwnedPurchaseDomainMapper
) {
    fun toDomain(result: BillingPurchaseResultDataModel): PurchaseSheetResultDomainModel =
        when (result) {
            is BillingPurchaseResultDataModel.Completed -> PurchaseSheetResultDomainModel.Completed(
                result.purchases.map(ownedPurchaseDomainMapper::toDomain)
            )
            BillingPurchaseResultDataModel.Cancelled -> PurchaseSheetResultDomainModel.Cancelled
            BillingPurchaseResultDataModel.AlreadyOwned ->
                PurchaseSheetResultDomainModel.AlreadyOwned
            BillingPurchaseResultDataModel.Unavailable -> PurchaseSheetResultDomainModel.Unavailable
            BillingPurchaseResultDataModel.Failed -> PurchaseSheetResultDomainModel.Failed
            BillingPurchaseResultDataModel.TimedOut -> PurchaseSheetResultDomainModel.Unanswered
        }
}

class PaywallTriggerDataMapper {
    fun toData(trigger: PaywallTriggerDomainModel): PaywallTriggerDataModel = when (trigger) {
        PaywallTriggerDomainModel.Onboarding -> PaywallTriggerDataModel.Onboarding
        PaywallTriggerDomainModel.FifthMigraine -> PaywallTriggerDataModel.FifthMigraine
    }
}
