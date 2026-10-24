package com.migrely.app.report.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.report.domain.model.CreateReportRequestDomainModel
import com.migrely.app.report.domain.model.CreateReportResultDomainModel
import com.migrely.app.report.domain.model.ReportDomainModel
import com.migrely.app.report.domain.model.ReportRecordsResultDomainModel
import com.migrely.app.report.domain.model.ReportWriteResultDomainModel
import com.migrely.app.report.domain.repository.CleanUpReportsRepository
import com.migrely.app.report.domain.repository.GetCurrentTimeRepository
import com.migrely.app.report.domain.repository.GetReportRecordsRepository
import com.migrely.app.report.domain.repository.WriteReportRepository

/**
 * Makes the doctor report for a period (BRD REP-F1–F4, EC-REP-01–03, D81–D85). Leftovers of earlier reports go first.
 * Notes are part of it only when asked for, since they are personal.
 */
class CreateReportUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getReportRecordsRepository: GetReportRecordsRepository,
    private val writeReportRepository: WriteReportRepository,
    private val cleanUpReportsRepository: CleanUpReportsRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<CreateReportRequestDomainModel, CreateReportResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(
        request: CreateReportRequestDomainModel
    ): CreateReportResultDomainModel {
        cleanUpReportsRepository.cleanUpReports()
        val now = getCurrentTimeRepository.currentTime()
        val today = now.instant.atZone(now.zone).toLocalDate()
        val dates = request.period.datesOn(today)
        val records = when (val read = getReportRecordsRepository.reportRecords()) {
            is ReportRecordsResultDomainModel.Loaded -> read.records
            ReportRecordsResultDomainModel.Failed -> return CreateReportResultDomainModel.Failed
        }
        val calculator = ReportCalculator(dates, now.instant)
        if (!calculator.anythingLogged(records)) return CreateReportResultDomainModel.NothingLogged
        val report = ReportDomainModel(
            dates = dates,
            madeOn = today,
            summary = calculator.summary(records),
            months = calculator.months(records),
            attacks = calculator.attacks(records),
            medications = calculator.medications(records),
            notes = if (request.includeNotes) calculator.notes(records) else emptyList()
        )
        return when (val written = writeReportRepository.writeReport(report)) {
            is ReportWriteResultDomainModel.Written -> CreateReportResultDomainModel.Created(
                written.uri
            )
            ReportWriteResultDomainModel.StorageFull -> CreateReportResultDomainModel.StorageFull
            ReportWriteResultDomainModel.Failed -> CreateReportResultDomainModel.Failed
        }
    }
}
