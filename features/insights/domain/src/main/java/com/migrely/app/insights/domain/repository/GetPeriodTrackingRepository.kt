package com.migrely.app.insights.domain.repository

/** Whether "Track periods" is on (BRD D79); with it off, Insights says nothing about periods (EC-INS-03). */
interface GetPeriodTrackingRepository {
    fun periodTracking(): Boolean
}
