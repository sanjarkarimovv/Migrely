package com.migrely.app.report.domain.repository

import com.migrely.app.report.domain.model.ReportDomainModel
import com.migrely.app.report.domain.model.ReportWriteResultDomainModel

interface WriteReportRepository {
    fun writeReport(report: ReportDomainModel): ReportWriteResultDomainModel
}
