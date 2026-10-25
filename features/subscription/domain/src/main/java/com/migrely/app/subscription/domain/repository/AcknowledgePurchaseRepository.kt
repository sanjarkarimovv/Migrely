package com.migrely.app.subscription.domain.repository

/**
 * True when Google Play took the acknowledgement; it refunds a purchase not acknowledged in 3 days.
 */
interface AcknowledgePurchaseRepository {
    fun acknowledgePurchase(purchaseToken: String): Boolean
}
