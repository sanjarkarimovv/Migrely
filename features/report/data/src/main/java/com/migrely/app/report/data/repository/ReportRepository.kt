package com.migrely.app.report.data.repository

import com.migrely.app.datasource.backup.datasource.BackupDataSource
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.DataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.datasource.report.datasource.ReportDocumentDataSource
import com.migrely.app.datasource.report.model.ReportPaperDataModel
import com.migrely.app.report.data.mapper.ReportDocumentMapper
import com.migrely.app.report.data.mapper.ReportRecordsDomainMapper
import com.migrely.app.report.domain.model.CurrentTimeDomainModel
import com.migrely.app.report.domain.model.ReportDomainModel
import com.migrely.app.report.domain.model.ReportRecordsResultDomainModel
import com.migrely.app.report.domain.model.ReportWriteResultDomainModel
import com.migrely.app.report.domain.repository.CleanUpReportsRepository
import com.migrely.app.report.domain.repository.GetCurrentTimeRepository
import com.migrely.app.report.domain.repository.GetReportRecordsRepository
import com.migrely.app.report.domain.repository.WriteReportRepository
import java.util.Locale

/** Countries that print on Letter paper rather than A4 (BRD D82). */
private val LETTER_COUNTRIES = setOf("US", "CA")

/** The doctor report (BRD REP-F1–F4, D81–D85): records from the diary, the file in the app's cache. */
class ReportRepository(
    private val backupDataSource: BackupDataSource,
    private val reportDocumentDataSource: ReportDocumentDataSource,
    private val clockDataSource: ClockDataSource,
    private val reportRecordsDomainMapper: ReportRecordsDomainMapper,
    private val reportDocumentMapper: ReportDocumentMapper,
    private val locale: () -> Locale = Locale::getDefault
) : GetCurrentTimeRepository,
    GetReportRecordsRepository,
    WriteReportRepository,
    CleanUpReportsRepository {
    override fun currentTime() =
        CurrentTimeDomainModel(clockDataSource.now(), clockDataSource.zone())

    override fun reportRecords(): ReportRecordsResultDomainModel = try {
        ReportRecordsResultDomainModel.Loaded(
            reportRecordsDomainMapper.toDomain(backupDataSource.records())
        )
    } catch (_: DataException) {
        ReportRecordsResultDomainModel.Failed
    }

    override fun writeReport(report: ReportDomainModel): ReportWriteResultDomainModel = try {
        val paper = if (locale().country in
            LETTER_COUNTRIES
        ) {
            ReportPaperDataModel.Letter
        } else {
            ReportPaperDataModel.A4
        }
        ReportWriteResultDomainModel.Written(
            reportDocumentDataSource.write(
                reportDocumentMapper.toData(report, paper, clockDataSource.zone())
            )
        )
    } catch (_: StorageFullDataException) {
        ReportWriteResultDomainModel.StorageFull
    } catch (_: DataException) {
        ReportWriteResultDomainModel.Failed
    }

    /** A clean-up that fails is tried again with the next report or the next start. */
    override fun cleanUpReports() {
        try {
            reportDocumentDataSource.cleanUp(clockDataSource.now())
        } catch (_: DataException) {
            // Nothing to report: the files are left for the next try.
        }
    }
}
