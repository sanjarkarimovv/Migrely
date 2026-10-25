package com.migrely.app.datasource.billing.mapper

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.migrely.app.datasource.billing.model.BillingOfferDataModel
import com.migrely.app.datasource.billing.model.BillingProductDataModel
import com.migrely.app.datasource.billing.model.BillingProductKindDataModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class PlayProductDetailsMapperTest {
    private val classUnderTest = PlayProductDetailsMapper()

    @Test
    fun `Given a trial when mapping then each plan keeps its recurring price`() {
        val offers = listOf(
            offer("yearly", null, "t-yearly", phase("€29.99", 29_990_000L, "P1Y")),
            offer(
                "yearly",
                "free-trial-7d",
                "t-trial",
                phase("Free", 0L, "P7D"),
                phase("€29.99", 29_990_000L, "P1Y")
            ),
            offer("monthly", null, "t-monthly", phase("€4.99", 4_990_000L, "P1M"))
        )
        val details = mock<ProductDetails> {
            on { productId } doReturn "migrely_premium"
            on { productType } doReturn BillingClient.ProductType.SUBS
            on { subscriptionOfferDetails } doReturn offers
        }

        assertEquals(
            BillingProductDataModel(
                "migrely_premium",
                BillingProductKindDataModel.Subscription,
                listOf(
                    BillingOfferDataModel("yearly", null, "t-yearly", "€29.99", "P1Y", null),
                    BillingOfferDataModel(
                        "yearly",
                        "free-trial-7d",
                        "t-trial",
                        "€29.99",
                        "P1Y",
                        "P7D"
                    ),
                    BillingOfferDataModel("monthly", null, "t-monthly", "€4.99", "P1M", null)
                )
            ),
            classUnderTest.toData(details)
        )
    }

    @Test
    fun `Given a one-time product when mapping then it has one price and no period`() {
        val oneTime = mock<ProductDetails.OneTimePurchaseOfferDetails> {
            on { offerId } doReturn null
            on { offerToken } doReturn "t-lifetime"
            on { formattedPrice } doReturn "€59.99"
        }
        val details = mock<ProductDetails> {
            on { productId } doReturn "migrely_lifetime"
            on { productType } doReturn BillingClient.ProductType.INAPP
            on { oneTimePurchaseOfferDetails } doReturn oneTime
        }

        assertEquals(
            BillingProductDataModel(
                "migrely_lifetime",
                BillingProductKindDataModel.OneTime,
                listOf(BillingOfferDataModel(null, null, "t-lifetime", "€59.99", null, null))
            ),
            classUnderTest.toData(details)
        )
    }

    @Test
    fun `Given a one-time product without a price when mapping then it is left out`() {
        val details = mock<ProductDetails> {
            on { productType } doReturn BillingClient.ProductType.INAPP
            on { oneTimePurchaseOfferDetails } doReturn null
        }

        assertNull(classUnderTest.toData(details))
    }

    private fun offer(
        basePlan: String,
        offer: String?,
        token: String,
        vararg phases: ProductDetails.PricingPhase
    ): ProductDetails.SubscriptionOfferDetails {
        val phaseList = phases.toList()
        val pricingPhases = mock<ProductDetails.PricingPhases> {
            on { pricingPhaseList } doReturn phaseList
        }
        return mock {
            on { basePlanId } doReturn basePlan
            on { offerId } doReturn offer
            on { offerToken } doReturn token
            on { this.pricingPhases } doReturn pricingPhases
        }
    }

    private fun phase(price: String, micros: Long, period: String) =
        mock<ProductDetails.PricingPhase> {
            on { formattedPrice } doReturn price
            on { priceAmountMicros } doReturn micros
            on { billingPeriod } doReturn period
        }
}
