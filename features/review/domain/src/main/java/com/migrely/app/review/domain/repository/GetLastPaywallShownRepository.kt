package com.migrely.app.review.domain.repository

import java.time.Instant

/** When a paywall last showed itself, or null if none ever did (BRD D110, D130). */
interface GetLastPaywallShownRepository {
    fun lastPaywallShown(): Instant?
}
