package com.migrely.app.history.domain.repository

/**
 * Whether Migrely Premium is on, as the last check with Google Play left it (BRD D108, change
 * add-subscription design D4).
 */
interface HasPremiumRepository {
    fun hasPremium(): Boolean
}
