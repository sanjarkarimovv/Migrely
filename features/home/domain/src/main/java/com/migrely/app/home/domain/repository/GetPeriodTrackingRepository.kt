package com.migrely.app.home.domain.repository

/** "Track periods" (BRD DAY-F1, D79). */
interface GetPeriodTrackingRepository {
    fun periodTracking(): Boolean
}
