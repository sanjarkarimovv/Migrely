package com.migrely.app.subscription.domain.repository

import java.time.Instant

/** The trial end whose banner was dismissed, or null. */
interface GetTrialBannerDismissalRepository {
    fun dismissedTrialEnd(): Instant?
}
