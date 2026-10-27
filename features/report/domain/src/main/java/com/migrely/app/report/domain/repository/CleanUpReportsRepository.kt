package com.migrely.app.report.domain.repository

/** Removes half-written reports and reports made more than a day ago (BRD EC-REP-03, D85). */
interface CleanUpReportsRepository {
    fun cleanUpReports()
}
