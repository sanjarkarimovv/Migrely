package com.migrely.app.review.data.repository

import com.migrely.app.datasource.preferences.datasource.UnsavedMarksDataSource
import com.migrely.app.datasource.preferences.model.PaywallTriggerDataModel

/** The app's in-memory store of unsaved marks (#278), for tests: a new instance is a new process. */
class FakeUnsavedMarksDataSource : UnsavedMarksDataSource {
    private val paywallShown = mutableSetOf<PaywallTriggerDataModel>()
    private var lastPaywallShownMillis: Long? = null
    private val reviewRequestsMillis = mutableListOf<Long>()
    private var lastSafetyCardShownMillis: Long? = null

    override fun paywallShown(): Set<PaywallTriggerDataModel> = paywallShown.toSet()

    override fun lastPaywallShownMillis(): Long? = lastPaywallShownMillis

    override fun keepPaywallShown(trigger: PaywallTriggerDataModel, atMillis: Long) {
        paywallShown += trigger
        lastPaywallShownMillis = maxOf(lastPaywallShownMillis ?: atMillis, atMillis)
    }

    override fun reviewRequestsMillis(): List<Long> = reviewRequestsMillis.toList()

    override fun keepReviewRequested(atMillis: Long) {
        reviewRequestsMillis += atMillis
    }

    override fun lastSafetyCardShownMillis(): Long? = lastSafetyCardShownMillis

    override fun keepSafetyCardShown(atMillis: Long) {
        lastSafetyCardShownMillis = maxOf(lastSafetyCardShownMillis ?: atMillis, atMillis)
    }
}
