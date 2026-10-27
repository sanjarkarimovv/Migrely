package com.migrely.app.subscription.data.mapper

/**
 * Migrely's products as created in Play Console (BRD D107, docs/release/play-products.md). These
 * ids are Play's and must never change.
 */
internal object SubscriptionProducts {
    const val PREMIUM = "migrely_premium"
    const val LIFETIME = "migrely_lifetime"
    const val YEARLY = "yearly"
    const val MONTHLY = "monthly"
    const val TRIAL_OFFER = "free-trial-7d"
}
