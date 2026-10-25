package com.migrely.app.subscription.domain.repository

import com.migrely.app.subscription.domain.model.BillingAvailabilityDomainModel

/**
 * Whether Migrely Premium can be bought here, for a paywall that shows itself (BRD D110): Google
 * Play billing connects and offers Migrely's products. The answer may be one Play gave a little
 * earlier.
 */
interface GetBillingAvailabilityRepository {
    fun billingAvailability(): BillingAvailabilityDomainModel
}
