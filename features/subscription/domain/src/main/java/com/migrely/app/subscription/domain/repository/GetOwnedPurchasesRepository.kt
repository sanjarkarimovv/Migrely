package com.migrely.app.subscription.domain.repository

import com.migrely.app.subscription.domain.model.OwnedPurchasesResultDomainModel

/** What Google Play reports this account owns now. */
interface GetOwnedPurchasesRepository {
    fun ownedPurchases(): OwnedPurchasesResultDomainModel
}
