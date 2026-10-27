package com.migrely.app.subscription.domain.repository

import com.migrely.app.subscription.domain.model.PremiumProductsResultDomainModel

/** The products in the order the paywall shows them: yearly, monthly, lifetime. */
interface GetPremiumProductsRepository {
    fun premiumProducts(): PremiumProductsResultDomainModel
}
