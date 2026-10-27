package com.migrely.app.subscription.domain.repository

import com.migrely.app.subscription.domain.model.LaunchedPurchaseDomainModel

/** The purchase last asked of Google Play's sheet, kept until a check gives it its purchase. */
interface GetLaunchedPurchaseRepository {
    fun launchedPurchase(): LaunchedPurchaseDomainModel?
}
