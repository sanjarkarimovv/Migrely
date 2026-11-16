package com.migrely.app.report.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.report.domain.model.ReportOptionsDomainModel
import com.migrely.app.report.domain.model.ReportPeriodDomainModel
import com.migrely.app.report.domain.model.ReportRecordsResultDomainModel
import com.migrely.app.report.domain.repository.GetCurrentTimeRepository
import com.migrely.app.report.domain.repository.GetReportCreditRepository
import com.migrely.app.report.domain.repository.GetReportRecordsRepository

/**
 * The dates a period covers, and whether anything was logged in them (BRD REP-F1, EC-REP-01, D81). The default period
 * is made concrete here, and the last visit is given when "Since last visit" is offered (EC-REP-04). The credit switch
 * comes with them, so it shows only once its value is read (REP-F5, EC-REP-13).
 */
class GetReportOptionsUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getReportRecordsRepository: GetReportRecordsRepository,
    private val getReportCreditRepository: GetReportCreditRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<ReportPeriodDomainModel, ReportOptionsDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(request: ReportPeriodDomainModel): ReportOptionsDomainModel {
        val now = getCurrentTimeRepository.currentTime()
        val today = now.instant.atZone(now.zone).toLocalDate()
        return when (val records = getReportRecordsRepository.reportRecords()) {
            is ReportRecordsResultDomainModel.Loaded -> {
                val lastVisit = DefaultReportPeriod.lastVisit(records.records.visitDates, today)
                val period = DefaultReportPeriod.resolve(request, lastVisit, today)
                val dates = period.datesOn(today)
                ReportOptionsDomainModel.Ready(
                    dates,
                    ReportCalculator(dates, now.instant).anythingLogged(records.records),
                    period,
                    lastVisit,
                    getReportCreditRepository.reportCreditShown()
                )
            }
            ReportRecordsResultDomainModel.Failed -> ReportOptionsDomainModel.Failed
        }
    }
}
