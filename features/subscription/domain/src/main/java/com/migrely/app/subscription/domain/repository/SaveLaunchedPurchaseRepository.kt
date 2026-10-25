package com.migrely.app.subscription.domain.repository

import com.migrely.app.subscription.domain.model.LaunchedPurchaseDomainModel

/** Keeps what is asked of Google Play's sheet before it opens; null forgets it (review M5). */
interface SaveLaunchedPurchaseRepository {
    fun saveLaunchedPurchase(launched: LaunchedPurchaseDomainModel?)
}
