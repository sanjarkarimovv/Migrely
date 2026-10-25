package com.migrely.app.datasource.billing.mapper

import com.android.billingclient.api.Purchase
import com.migrely.app.datasource.billing.model.BillingPurchaseDataModel
import com.migrely.app.datasource.billing.model.BillingPurchaseStateDataModel
import com.migrely.app.datasource.billing.security.PurchaseSignatureVerifier

/** A Google Play purchase, with whether its signature holds. */
class PlayPurchaseMapper(private val signatureVerifier: PurchaseSignatureVerifier) {
    fun toData(purchase: Purchase) = BillingPurchaseDataModel(
        productIds = purchase.products,
        state = when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> BillingPurchaseStateDataModel.Purchased
            Purchase.PurchaseState.PENDING -> BillingPurchaseStateDataModel.Pending
            else -> BillingPurchaseStateDataModel.Unspecified
        },
        purchaseTimeMillis = purchase.purchaseTime,
        autoRenewing = purchase.isAutoRenewing,
        purchaseToken = purchase.purchaseToken,
        acknowledged = purchase.isAcknowledged,
        signatureValid = signatureVerifier.isValid(purchase.originalJson, purchase.signature)
    )
}
