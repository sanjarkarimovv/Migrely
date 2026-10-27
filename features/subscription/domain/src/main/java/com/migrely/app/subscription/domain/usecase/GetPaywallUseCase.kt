package com.migrely.app.subscription.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.subscription.domain.model.PaywallDomainModel
import com.migrely.app.subscription.domain.model.PaywallOfferDomainModel
import com.migrely.app.subscription.domain.model.PaywallOffersDomainModel
import com.migrely.app.subscription.domain.model.PremiumPlanDomainModel
import com.migrely.app.subscription.domain.model.PremiumProductDomainModel
import com.migrely.app.subscription.domain.model.PremiumProductsResultDomainModel
import com.migrely.app.subscription.domain.model.PremiumStatusDomainModel
import com.migrely.app.subscription.domain.repository.GetPremiumProductsRepository

/**
 * What the paywall offers (BRD §7.3, D109): the status and Google Play's products. A subscriber can
 * move from monthly to yearly, or buy lifetime; a move to a cheaper plan is made in Google Play,
 * and so is any change to a subscription whose plan is unknown on this phone.
 */
class GetPaywallUseCase(
    private val premiumChecker: PremiumChecker,
    private val getPremiumProductsRepository: GetPremiumProductsRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, PaywallDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): PaywallDomainModel {
        val status = premiumChecker.current()
        val offers = when (val products = getPremiumProductsRepository.premiumProducts()) {
            is PremiumProductsResultDomainModel.Loaded ->
                PaywallOffersDomainModel.Loaded(products.products.map { offer(it, status) })
            PremiumProductsResultDomainModel.Unavailable -> PaywallOffersDomainModel.Unavailable
            PremiumProductsResultDomainModel.Unreachable -> PaywallOffersDomainModel.Unreachable
        }
        return PaywallDomainModel(status, offers)
    }

    private fun offer(
        product: PremiumProductDomainModel,
        status: PremiumStatusDomainModel
    ): PaywallOfferDomainModel {
        val plan = product.plan
        val lifetime = plan == PremiumPlanDomainModel.Lifetime
        val (current, choosable) = when (status) {
            PremiumStatusDomainModel.Free, PremiumStatusDomainModel.Pending -> false to true
            is PremiumStatusDomainModel.Subscribed -> when (status.plan) {
                // Monthly moves to yearly or lifetime; a cheaper plan is changed in Google Play.
                PremiumPlanDomainModel.Monthly -> (plan == status.plan) to (plan != status.plan)
                PremiumPlanDomainModel.Yearly -> (plan == status.plan) to lifetime
                // Bought on another phone: which plan is unknown, so only lifetime (review M9).
                PremiumPlanDomainModel.Lifetime, null -> false to lifetime
            }
            is PremiumStatusDomainModel.Lifetime -> lifetime to false
        }
        return PaywallOfferDomainModel(product, current = current, choosable = choosable)
    }
}
