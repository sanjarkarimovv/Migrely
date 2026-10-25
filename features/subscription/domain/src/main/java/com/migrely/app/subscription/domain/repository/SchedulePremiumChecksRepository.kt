package com.migrely.app.subscription.domain.repository

/**
 * Plans background checks with Google Play while [needed], and cancels them once not (review I2).
 * A failure to plan is not reported: the next check tries again.
 */
interface SchedulePremiumChecksRepository {
    fun schedulePremiumChecks(needed: Boolean)
}
