package com.migrely.app.datasource.billing.mapper

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.migrely.app.datasource.billing.model.BillingOfferDataModel
import com.migrely.app.datasource.billing.model.BillingProductDataModel
import com.migrely.app.datasource.billing.model.BillingProductKindDataModel

/**
 * Google Play's product details as the app needs them. A subscription offer's free phase is a
 * pricing phase that costs nothing; its recurring price is the last phase, the one that repeats.
 */
class PlayProductDetailsMapper {
    fun toData(details: ProductDetails): BillingProductDataModel? = when (details.productType) {
        BillingClient.ProductType.SUBS -> BillingProductDataModel(
            productId = details.productId,
            kind = BillingProductKindDataModel.Subscription,
            offers = details.subscriptionOfferDetails.orEmpty().mapNotNull(::subscriptionOffer)
        )

        BillingClient.ProductType.INAPP -> details.oneTimePurchaseOfferDetails?.let { offer ->
            BillingProductDataModel(
                productId = details.productId,
                kind = BillingProductKindDataModel.OneTime,
                offers = listOf(
                    BillingOfferDataModel(
                        basePlanId = null,
                        offerId = offer.offerId,
                        offerToken = offer.offerToken,
                        formattedPrice = offer.formattedPrice,
                        billingPeriod = null,
                        freeTrialPeriod = null
                    )
                )
            )
        }

        else -> null
    }

    private fun subscriptionOffer(
        offer: ProductDetails.SubscriptionOfferDetails
    ): BillingOfferDataModel? {
        val phases = offer.pricingPhases.pricingPhaseList
        val recurring = phases.lastOrNull() ?: return null
        val freePhase = phases.firstOrNull { it.priceAmountMicros == 0L && it != recurring }
        return BillingOfferDataModel(
            basePlanId = offer.basePlanId,
            offerId = offer.offerId,
            offerToken = offer.offerToken,
            formattedPrice = recurring.formattedPrice,
            billingPeriod = recurring.billingPeriod,
            freeTrialPeriod = freePhase?.billingPeriod
        )
    }
}
