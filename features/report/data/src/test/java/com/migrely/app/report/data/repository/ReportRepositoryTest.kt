package com.migrely.app.report.data.repository

import com.migrely.app.datasource.backup.datasource.BackupDataSource
import com.migrely.app.datasource.backup.model.BackupRecordsDataModel
import com.migrely.app.datasource.clock.datasource.ClockDataSource
import com.migrely.app.datasource.exception.DatabaseDataException
import com.migrely.app.datasource.exception.FileDataException
import com.migrely.app.datasource.exception.StorageFullDataException
import com.migrely.app.datasource.report.datasource.ReportDocumentDataSource
import com.migrely.app.datasource.report.model.ReportDocumentDataModel
import com.migrely.app.datasource.report.model.ReportPaperDataModel
import com.migrely.app.report.data.mapper.ReportDateTimeFormatter
import com.migrely.app.report.data.mapper.ReportDocumentMapper
import com.migrely.app.report.data.mapper.ReportRecordsDomainMapper
import com.migrely.app.report.domain.model.ReportDatesDomainModel
import com.migrely.app.report.domain.model.ReportDomainModel
import com.migrely.app.report.domain.model.ReportRecordsDomainModel
import com.migrely.app.report.domain.model.ReportRecordsResultDomainModel
import com.migrely.app.report.domain.model.ReportSummaryDomainModel
import com.migrely.app.report.domain.model.ReportWriteResultDomainModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

private val NOW = Instant.parse("2026-10-25T07:00:00Z")
private val REPORT = ReportDomainModel(
    ReportDatesDomainModel(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 25)),
    LocalDate.of(2026, 10, 25),
    ReportSummaryDomainModel(25, 0, 0.0, 0, 0, 0.0, emptyList(), 0, null, false),
    emptyList(),
    emptyList(),
    emptyList(),
    emptyList()
)

class ReportRepositoryTest {
    private val backupDataSource: BackupDataSource = mock()
    private val reportDocumentDataSource: ReportDocumentDataSource = mock()
    private val clockDataSource: ClockDataSource = mock {
        on { now() } doReturn NOW
        on { zone() } doReturn ZoneId.of("Asia/Tashkent")
    }
    private var locale: Locale = Locale.UK
    private val classUnderTest = ReportRepository(
        backupDataSource,
        reportDocumentDataSource,
        clockDataSource,
        ReportRecordsDomainMapper(),
        ReportDocumentMapper(ReportDateTimeFormatter { locale })
    ) { locale }

    @Test
    fun `When reading the records then the diary's, or failed`() {
        given(backupDataSource.records()).willReturn(
            BackupRecordsDataModel(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        )
        assertEquals(
            ReportRecordsResultDomainModel.Loaded(
                ReportRecordsDomainModel(emptyList(), emptyList(), emptySet())
            ),
            classUnderTest.reportRecords()
        )

        given(backupDataSource.records()).willThrow(DatabaseDataException())
        assertEquals(ReportRecordsResultDomainModel.Failed, classUnderTest.reportRecords())
    }

    @Test
    fun `When writing then A4, or Letter in the US and Canada`() {
        given(reportDocumentDataSource.write(any())).willReturn("content://r")
        val documents = argumentCaptor<ReportDocumentDataModel>()

        assertEquals(
            ReportWriteResultDomainModel.Written("content://r"),
            classUnderTest.writeReport(REPORT)
        )
        locale = Locale.US
        classUnderTest.writeReport(REPORT)
        locale = Locale.CANADA_FRENCH
        classUnderTest.writeReport(REPORT)

        verify(reportDocumentDataSource, times(3)).write(documents.capture())
        assertEquals(
            listOf(
                ReportPaperDataModel.A4,
                ReportPaperDataModel.Letter,
                ReportPaperDataModel.Letter
            ),
            documents.allValues.map { it.paper }
        )
    }

    @Test
    fun `Given the write fails then storage full or failed, and a failed clean-up is quiet`() {
        whenever(reportDocumentDataSource.write(any())).doThrow(StorageFullDataException())
        assertEquals(ReportWriteResultDomainModel.StorageFull, classUnderTest.writeReport(REPORT))

        whenever(reportDocumentDataSource.write(any())).doThrow(FileDataException())
        assertEquals(ReportWriteResultDomainModel.Failed, classUnderTest.writeReport(REPORT))

        whenever(reportDocumentDataSource.cleanUp(NOW)).doThrow(FileDataException())
        classUnderTest.cleanUpReports()
        verify(reportDocumentDataSource).cleanUp(NOW)
    }
}
