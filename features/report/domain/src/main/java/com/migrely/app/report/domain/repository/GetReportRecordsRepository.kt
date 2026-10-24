package com.migrely.app.report.domain.repository

import com.migrely.app.report.domain.model.ReportRecordsResultDomainModel

/** Every health record but attacks being deleted; the report keeps its period's. */
interface GetReportRecordsRepository {
    fun reportRecords(): ReportRecordsResultDomainModel
}
