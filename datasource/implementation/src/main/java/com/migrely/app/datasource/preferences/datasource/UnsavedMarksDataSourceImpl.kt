package com.migrely.app.datasource.preferences.datasource

import com.migrely.app.datasource.preferences.model.PaywallTriggerDataModel

/** In memory, one for the app; reads and writes come from background threads, so each holds the lock. */
class UnsavedMarksDataSourceImpl : UnsavedMarksDataSource {
    private val paywallShown = mutableSetOf<PaywallTriggerDataModel>()
    private var lastPaywallShownMillis: Long? = null
    private val reviewRequestsMillis = mutableListOf<Long>()
    private var lastSafetyCardShownMillis: Long? = null

    @Synchronized
    override fun paywallShown(): Set<PaywallTriggerDataModel> = paywallShown.toSet()

    @Synchronized
    override fun lastPaywallShownMillis(): Long? = lastPaywallShownMillis

    @Synchronized
    override fun keepPaywallShown(trigger: PaywallTriggerDataModel, atMillis: Long) {
        paywallShown += trigger
        lastPaywallShownMillis = maxOf(lastPaywallShownMillis ?: atMillis, atMillis)
    }

    @Synchronized
    override fun reviewRequestsMillis(): List<Long> = reviewRequestsMillis.toList()

    @Synchronized
    override fun keepReviewRequested(atMillis: Long) {
        reviewRequestsMillis += atMillis
    }

    @Synchronized
    override fun lastSafetyCardShownMillis(): Long? = lastSafetyCardShownMillis

    @Synchronized
    override fun keepSafetyCardShown(atMillis: Long) {
        lastSafetyCardShownMillis = maxOf(lastSafetyCardShownMillis ?: atMillis, atMillis)
    }
}
