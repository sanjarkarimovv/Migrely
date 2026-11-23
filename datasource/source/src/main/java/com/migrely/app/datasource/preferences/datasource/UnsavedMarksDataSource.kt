package com.migrely.app.datasource.preferences.datasource

import com.migrely.app.datasource.preferences.model.PaywallTriggerDataModel

/**
 * Paywall, rating-request and safety-card marks whose write did not reach the phone, kept for this process only (#278,
 * BRD D197, D188): a failed write leaves the preferences as stored, and without these the paywall or the rating request
 * would open on every Home load while the storage stays full, and a rating could follow a safety card. They go with the
 * process; after a restart only the stored marks count.
 */
interface UnsavedMarksDataSource {
    fun paywallShown(): Set<PaywallTriggerDataModel>

    fun lastPaywallShownMillis(): Long?

    fun keepPaywallShown(trigger: PaywallTriggerDataModel, atMillis: Long)

    /** The times of rating requests that were not saved, oldest first. */
    fun reviewRequestsMillis(): List<Long>

    fun keepReviewRequested(atMillis: Long)

    fun lastSafetyCardShownMillis(): Long?

    fun keepSafetyCardShown(atMillis: Long)
}
