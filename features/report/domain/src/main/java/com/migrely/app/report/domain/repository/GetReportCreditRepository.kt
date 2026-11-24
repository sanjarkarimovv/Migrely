package com.migrely.app.report.domain.repository

/** Whether the report names Migrely on its last page; on until the user turns it off (BRD REP-F5, D186). */
interface GetReportCreditRepository {
    fun reportCreditShown(): Boolean
}
