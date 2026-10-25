package com.migrely.app.subscription.domain.repository

import com.migrely.app.subscription.domain.model.PurchaseLaunchDomainModel
import com.migrely.app.subscription.domain.model.PurchaseSheetResultDomainModel

/** Opens Google Play's purchase sheet and waits until it closes. */
interface LaunchPurchaseRepository {
    fun launchPurchase(launch: PurchaseLaunchDomainModel): PurchaseSheetResultDomainModel
}
