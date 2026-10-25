package com.migrely.app.datasource.schedule.datasource

/**
 * Background checks of Premium with Google Play, about every 12 hours and with no constraints,
 * while a payment is pending or a purchase is not yet acknowledged, which Play refunds after 3 days
 * (change add-subscription, review I2). They need no activity and survive a reboot.
 */
interface PremiumCheckScheduleDataSource {
    /** Plans the checks; when they are planned already, the plan stays as it is. */
    fun schedule()

    /**
     * Cancels the planned checks and returns once that is stored, so it holds even if the app
     * stops right after (BRD D96). Throws
     * [com.migrely.app.datasource.exception.DatabaseDataException] when WorkManager cannot store it.
     */
    fun cancel()
}
