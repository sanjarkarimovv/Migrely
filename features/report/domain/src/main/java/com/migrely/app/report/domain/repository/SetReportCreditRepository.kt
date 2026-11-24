package com.migrely.app.report.domain.repository

import com.migrely.app.report.domain.model.ReportCreditWriteResultDomainModel

interface SetReportCreditRepository {
    fun setReportCreditShown(on: Boolean): ReportCreditWriteResultDomainModel
}
