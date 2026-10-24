package com.migrely.app.history.domain.repository

/** "Track periods" (BRD DAY-F1, D79). */
interface GetPeriodTrackingRepository {
    fun periodTracking(): Boolean
}
