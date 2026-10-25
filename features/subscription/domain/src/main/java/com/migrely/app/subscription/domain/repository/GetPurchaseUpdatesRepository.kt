package com.migrely.app.subscription.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * A signal each time Google Play reports a purchase change that no purchase sheet waits for, such
 * as a pending payment completed at a shop (review I1).
 */
interface GetPurchaseUpdatesRepository {
    fun purchaseUpdates(): Flow<Unit>
}
