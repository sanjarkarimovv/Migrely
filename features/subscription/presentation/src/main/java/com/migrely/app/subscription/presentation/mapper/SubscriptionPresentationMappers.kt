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

class PremiumPlanPresentationMapper {
    fun toPresentation(plan: PremiumPlanDomainModel): PremiumPlanPresentationModel = when (plan) {
        PremiumPlanDomainModel.Yearly -> PremiumPlanPresentationModel.Yearly
        PremiumPlanDomainModel.Monthly -> PremiumPlanPresentationModel.Monthly
        PremiumPlanDomainModel.Lifetime -> PremiumPlanPresentationModel.Lifetime
    }
}

class PremiumStatusPresentationMapper(
    private val premiumPlanPresentationMapper: PremiumPlanPresentationMapper
) {
    fun toPresentation(status: PremiumStatusDomainModel): PremiumStatusPresentationModel =
        when (status) {
            PremiumStatusDomainModel.Free -> PremiumStatusPresentationModel.Free
            PremiumStatusDomainModel.Pending -> PremiumStatusPresentationModel.Pending
            is PremiumStatusDomainModel.Subscribed -> PremiumStatusPresentationModel.Subscribed(
                status.plan?.let(premiumPlanPresentationMapper::toPresentation),
                status.trialEnd
            )
            is PremiumStatusDomainModel.Lifetime ->
                PremiumStatusPresentationModel.Lifetime(status.subscriptionRenewsToo)
        }
}

/** What the paywall shows for Google Play's answer. */
class PaywallPresentationMapper(
    private val premiumStatusPresentationMapper: PremiumStatusPresentationMapper,
    private val premiumPlanPresentationMapper: PremiumPlanPresentationMapper
) {
    fun toPresentation(paywall: PaywallDomainModel): PaywallViewState =
        when (val offers = paywall.offers) {
            is PaywallOffersDomainModel.Loaded -> PaywallViewState.Ready(
                premiumStatusPresentationMapper.toPresentation(paywall.status),
                offers.offers.map(::product)
            )
            PaywallOffersDomainModel.Unavailable -> PaywallViewState.Unavailable
            PaywallOffersDomainModel.Unreachable -> PaywallViewState.Unreachable
        }

    private fun product(offer: PaywallOfferDomainModel) = PaywallProductPresentationModel(
        plan = premiumPlanPresentationMapper.toPresentation(offer.product.plan),
        price = offer.product.price,
        freeTrial = offer.product.freeTrial,
        offerToken = offer.product.offerToken,
        current = offer.current,
        choosable = offer.choosable
    )
}

/** The product card chosen, back to what Google Play is asked to sell. */
class PaywallProductDomainMapper {
    fun toDomain(product: PaywallProductPresentationModel) = PremiumProductDomainModel(
        plan = when (product.plan) {
            PremiumPlanPresentationModel.Yearly -> PremiumPlanDomainModel.Yearly
            PremiumPlanPresentationModel.Monthly -> PremiumPlanDomainModel.Monthly
            PremiumPlanPresentationModel.Lifetime -> PremiumPlanDomainModel.Lifetime
        },
        price = product.price,
        freeTrial = product.freeTrial,
        offerToken = product.offerToken
    )
}

/** The trigger a paywall that showed itself is marked with; none when it was tapped (D110). */
class PaywallTriggerDomainMapper {
    fun toDomain(context: PaywallContextPresentationModel): PaywallTriggerDomainModel? =
        when (context) {
            PaywallContextPresentationModel.Onboarding -> PaywallTriggerDomainModel.Onboarding
            PaywallContextPresentationModel.Patterns -> PaywallTriggerDomainModel.FifthMigraine
            PaywallContextPresentationModel.Report,
            PaywallContextPresentationModel.Insights,
            PaywallContextPresentationModel.Settings -> null
        }
}
