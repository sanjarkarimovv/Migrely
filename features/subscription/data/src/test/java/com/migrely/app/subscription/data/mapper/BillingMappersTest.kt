package com.migrely.app.subscription.data.mapper

import com.migrely.app.datasource.billing.model.BillingOfferDataModel
import com.migrely.app.datasource.billing.model.BillingProductDataModel
import com.migrely.app.datasource.billing.model.BillingProductKindDataModel
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
import org.junit.Assert.assertEquals
import org.junit.Test

private val YEARLY_BASE = BillingOfferDataModel("yearly", null, "t-yearly", "€29.99", "P1Y", null)
private val YEARLY_TRIAL =
    BillingOfferDataModel("yearly", "free-trial-7d", "t-trial", "€29.99", "P1Y", "P7D")
private val MONTHLY = BillingOfferDataModel("monthly", null, "t-monthly", "€4.99", "P1M", null)
private val LIFETIME = BillingOfferDataModel(null, null, "t-lifetime", "€59.99", null, null)

private fun premium(vararg offers: BillingOfferDataModel) = BillingProductDataModel(
    "migrely_premium",
    BillingProductKindDataModel.Subscription,
    offers.toList()
)

private val LIFETIME_PRODUCT =
    BillingProductDataModel(
        "migrely_lifetime",
        BillingProductKindDataModel.OneTime,
        listOf(LIFETIME)
    )

class BillingMappersTest {
    private val productsMapper = PremiumProductsDomainMapper()

    @Test
    fun `Given the trial is offered then yearly is the trial, then monthly and lifetime`() {
        val products = productsMapper.toDomain(
            listOf(premium(YEARLY_BASE, YEARLY_TRIAL, MONTHLY), LIFETIME_PRODUCT)
        )

        assertEquals(
            listOf(
                PremiumProductDomainModel(PremiumPlanDomainModel.Yearly, "€29.99", true, "t-trial"),
                PremiumProductDomainModel(
                    PremiumPlanDomainModel.Monthly,
                    "€4.99",
                    false,
                    "t-monthly"
                ),
                PremiumProductDomainModel(
                    PremiumPlanDomainModel.Lifetime,
                    "€59.99",
                    false,
                    "t-lifetime"
                )
            ),
            products
        )
    }

    @Test
    fun `Given the trial was used then yearly is its base plan with no trial`() {
        val products = productsMapper.toDomain(listOf(premium(YEARLY_BASE, MONTHLY)))

        assertEquals(
            PremiumProductDomainModel(PremiumPlanDomainModel.Yearly, "€29.99", false, "t-yearly"),
            products.first()
        )
    }

    @Test
    fun `Given Play offers nothing then there are no products`() {
        assertEquals(emptyList<PremiumProductDomainModel>(), productsMapper.toDomain(emptyList()))
    }

    @Test
    fun `When mapping purchases then the product and state are Migrely's own`() {
        val mapper = OwnedPurchaseDomainMapper()
        fun purchase(product: String, state: BillingPurchaseStateDataModel) = mapper.toDomain(
            BillingPurchaseDataModel(listOf(product), state, 1_000L, false, "t", true, true)
        )

        assertEquals(
            OwnedPurchaseDomainModel(
                PurchaseProductDomainModel.Lifetime,
                PurchaseStateDomainModel.Purchased,
                Instant.ofEpochMilli(1_000L),
                false,
                "t",
                acknowledged = true,
                signatureValid = true
            ),
            purchase("migrely_lifetime", BillingPurchaseStateDataModel.Purchased)
        )
        assertEquals(
            PurchaseProductDomainModel.Subscription,
            purchase("migrely_premium", BillingPurchaseStateDataModel.Pending).product
        )
        assertEquals(
            PurchaseStateDomainModel.Pending,
            purchase("migrely_premium", BillingPurchaseStateDataModel.Pending).state
        )
        assertEquals(
            PurchaseProductDomainModel.Other,
            purchase("something_else", BillingPurchaseStateDataModel.Unspecified).product
        )
    }

    @Test
    fun `When asking to buy then the product, offer and replaced purchase go to Play`() {
        val yearly = PremiumProductDomainModel(PremiumPlanDomainModel.Yearly, "€29.99", false, "t")

        assertEquals(
            BillingPurchaseRequestDataModel("migrely_premium", "t", "old"),
            PurchaseRequestDataMapper().toData(PurchaseLaunchDomainModel(yearly, "old"))
        )
        assertEquals(
            BillingPurchaseRequestDataModel("migrely_lifetime", "t", null),
            PurchaseRequestDataMapper().toData(
                PurchaseLaunchDomainModel(yearly.copy(plan = PremiumPlanDomainModel.Lifetime), null)
            )
        )
    }

    @Test
    fun `When the sheet closes then each outcome maps`() {
        val mapper = PurchaseSheetResultDomainMapper(OwnedPurchaseDomainMapper())

        assertEquals(
            PurchaseSheetResultDomainModel.Completed(emptyList()),
            mapper.toDomain(BillingPurchaseResultDataModel.Completed(emptyList()))
        )
        assertEquals(
            listOf(
                PurchaseSheetResultDomainModel.Cancelled,
                PurchaseSheetResultDomainModel.AlreadyOwned,
                PurchaseSheetResultDomainModel.Unavailable,
                PurchaseSheetResultDomainModel.Failed,
                PurchaseSheetResultDomainModel.Unanswered
            ),
            listOf(
                BillingPurchaseResultDataModel.Cancelled,
                BillingPurchaseResultDataModel.AlreadyOwned,
                BillingPurchaseResultDataModel.Unavailable,
                BillingPurchaseResultDataModel.Failed,
                BillingPurchaseResultDataModel.TimedOut
            ).map(mapper::toDomain)
        )
    }

    @Test
    fun `When marking a trigger then its stored key is Migrely's own`() {
        assertEquals(
            listOf(PaywallTriggerDataModel.Onboarding, PaywallTriggerDataModel.FifthMigraine),
            PaywallTriggerDomainModel.entries.map(PaywallTriggerDataMapper()::toData)
        )
    }
}
