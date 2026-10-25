package com.migrely.app.subscription.presentation.mapper

import com.migrely.app.subscription.domain.model.PaywallDomainModel
import com.migrely.app.subscription.domain.model.PaywallOfferDomainModel
import com.migrely.app.subscription.domain.model.PaywallOffersDomainModel
import com.migrely.app.subscription.domain.model.PaywallTriggerDomainModel
import com.migrely.app.subscription.domain.model.PremiumPlanDomainModel
import com.migrely.app.subscription.domain.model.PremiumProductDomainModel
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.presentation.model.PaywallContextPresentationModel
import com.migrely.app.subscription.presentation.model.PaywallProductPresentationModel
import com.migrely.app.subscription.presentation.model.PaywallViewState
import com.migrely.app.subscription.presentation.model.PremiumPlanPresentationModel
import com.migrely.app.subscription.presentation.model.PremiumStatusPresentationModel
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

private val TRIAL_END: Instant = Instant.parse("2026-10-26T09:00:00Z")

@RunWith(Parameterized::class)
class PremiumStatusPresentationMapperTest(
    private val status: PremiumStatusDomainModel,
    private val expected: PremiumStatusPresentationModel
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any>> = listOf(
            arrayOf(PremiumStatusDomainModel.Free, PremiumStatusPresentationModel.Free),
            arrayOf(PremiumStatusDomainModel.Pending, PremiumStatusPresentationModel.Pending),
            arrayOf(
                PremiumStatusDomainModel.Subscribed(PremiumPlanDomainModel.Yearly, TRIAL_END),
                PremiumStatusPresentationModel.Subscribed(
                    PremiumPlanPresentationModel.Yearly,
                    TRIAL_END
                )
            ),
            arrayOf(
                PremiumStatusDomainModel.Subscribed(null, null),
                PremiumStatusPresentationModel.Subscribed(null, null)
            ),
            arrayOf(
                PremiumStatusDomainModel.Lifetime(subscriptionRenewsToo = true),
                PremiumStatusPresentationModel.Lifetime(subscriptionRenewsToo = true)
            )
        )
    }

    @Test
    fun `When mapping the status then each state keeps what it shows`() {
        assertEquals(
            expected,
            PremiumStatusPresentationMapper(PremiumPlanPresentationMapper()).toPresentation(status)
        )
    }
}

class PaywallPresentationMappersTest {
    private val planMapper = PremiumPlanPresentationMapper()
    private val classUnderTest =
        PaywallPresentationMapper(PremiumStatusPresentationMapper(planMapper), planMapper)
    private val monthly =
        PremiumProductDomainModel(PremiumPlanDomainModel.Monthly, "€4.99", null, "t-monthly")
    private val monthlyCard = PaywallProductPresentationModel(
        PremiumPlanPresentationModel.Monthly,
        "€4.99",
        freeTrial = null,
        offerToken = "t-monthly",
        current = true,
        choosable = false
    )

    @Test
    fun `Given products when mapping then the cards keep Play's prices and the plan in use`() {
        val paywall = PaywallDomainModel(
            PremiumStatusDomainModel.Free,
            PaywallOffersDomainModel.Loaded(listOf(PaywallOfferDomainModel(monthly, true, false)))
        )

        assertEquals(
            PaywallViewState.Ready(PremiumStatusPresentationModel.Free, listOf(monthlyCard)),
            classUnderTest.toPresentation(paywall)
        )
    }

    @Test
    fun `Given no billing or no answer when mapping then the paywall says which`() {
        assertEquals(
            PaywallViewState.Unavailable,
            classUnderTest.toPresentation(
                PaywallDomainModel(
                    PremiumStatusDomainModel.Free,
                    PaywallOffersDomainModel.Unavailable
                )
            )
        )
        assertEquals(
            PaywallViewState.Unreachable,
            classUnderTest.toPresentation(
                PaywallDomainModel(
                    PremiumStatusDomainModel.Free,
                    PaywallOffersDomainModel.Unreachable
                )
            )
        )
    }

    @Test
    fun `When a card is chosen then it maps back to the product Play sells`() {
        assertEquals(monthly, PaywallProductDomainMapper().toDomain(monthlyCard))
    }

    @Test
    fun `Given each context then only the ones that show themselves are marked`() {
        assertEquals(
            listOf(
                PaywallTriggerDomainModel.Onboarding,
                PaywallTriggerDomainModel.FifthMigraine,
                null,
                null,
                null
            ),
            PaywallContextPresentationModel.entries.map(PaywallTriggerDomainMapper()::toDomain)
        )
    }
}
