package com.migrely.app.report.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.report.domain.model.CreateReportRequestDomainModel
import com.migrely.app.report.domain.model.CreateReportResultDomainModel
import com.migrely.app.report.domain.model.CurrentTimeDomainModel
import com.migrely.app.report.domain.model.ReportAttackDomainModel
import com.migrely.app.report.domain.model.ReportDomainModel
import com.migrely.app.report.domain.model.ReportOptionsDomainModel
import com.migrely.app.report.domain.model.ReportPeriodDomainModel
import com.migrely.app.report.domain.model.ReportRecordsDomainModel
import com.migrely.app.report.domain.model.ReportRecordsResultDomainModel
import com.migrely.app.report.domain.model.ReportWriteResultDomainModel
import com.migrely.app.report.domain.repository.CleanUpReportsRepository
import com.migrely.app.report.domain.repository.GetCurrentTimeRepository
import com.migrely.app.report.domain.repository.GetReportRecordsRepository
import com.migrely.app.report.domain.repository.WriteReportRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

private val ZONE: ZoneId = ZoneId.of("Asia/Tashkent")
private val NOW: Instant = Instant.parse("2026-10-25T07:00:00Z")
private val ATTACK = ReportAttackDomainModel(
    Instant.parse(
        "2026-10-20T04:00:00Z"
    ),
    ZONE, Instant.parse("2026-10-20T06:00:00Z"), 7, 3, "A note",
    emptyList(), emptyList(), emptyList(), emptyList()
)
private val THREE_MONTHS = ReportPeriodDomainModel.LastMonths(3)

class CreateReportUseCaseTest {
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock {
        on { currentTime() } doReturn CurrentTimeDomainModel(NOW, ZONE)
    }
    private val getReportRecordsRepository: GetReportRecordsRepository = mock()
    private val writeReportRepository: WriteReportRepository = mock()
    private val cleanUpReportsRepository: CleanUpReportsRepository = mock()
    private val classUnderTest = CreateReportUseCase(
        getCurrentTimeRepository,
        getReportRecordsRepository,
        writeReportRepository,
        cleanUpReportsRepository,
        CoroutineContextProvider.Default
    )

    private fun givenRecords(vararg attacks: ReportAttackDomainModel) =
        given(getReportRecordsRepository.reportRecords())
            .willReturn(
                ReportRecordsResultDomainModel.Loaded(
                    ReportRecordsDomainModel(attacks.toList(), emptyList(), emptySet())
                )
            )

    @Test
    fun `When creating then old reports go first, and the written file's URI comes back`() {
        givenRecords(ATTACK)
        given(
            writeReportRepository.writeReport(any())
        ).willReturn(ReportWriteResultDomainModel.Written("content://r"))

        assertEquals(
            CreateReportResultDomainModel.Created("content://r"),
            classUnderTest.executeInBackground(
                CreateReportRequestDomainModel(THREE_MONTHS, includeNotes = true)
            )
        )

        val report = argumentCaptor<ReportDomainModel>()
        inOrder(cleanUpReportsRepository, writeReportRepository) {
            verify(cleanUpReportsRepository).cleanUpReports()
            verify(writeReportRepository).writeReport(report.capture())
        }
        assertEquals(LocalDate.of(2026, 10, 25), report.firstValue.madeOn)
        assertEquals(1, report.firstValue.summary.bedRestAttacks)
        assertEquals(listOf("A note"), report.firstValue.notes.map { it.note })
    }

    @Test
    fun `Given notes then each points at its own migraine in the report's migraines`() {
        givenRecords(
            ATTACK,
            ATTACK.copy(start = Instant.parse("2026-10-22T04:00:00Z"), note = null),
            ATTACK.copy(start = Instant.parse("2026-10-24T04:00:00Z"), note = "Newest")
        )
        given(
            writeReportRepository.writeReport(any())
        ).willReturn(ReportWriteResultDomainModel.Written("content://r"))

        classUnderTest.executeInBackground(
            CreateReportRequestDomainModel(THREE_MONTHS, includeNotes = true)
        )

        val report = argumentCaptor<ReportDomainModel>()
        verify(writeReportRepository).writeReport(report.capture())
        // The writer keeps a note only while its migraine's row is kept (D84).
        assertEquals(
            listOf("Newest", "A note"),
            report.firstValue.notes.map { report.firstValue.attacks[it.attackIndex].note }
        )
    }

    @Test
    fun `Given notes are not asked for then the report holds none`() {
        givenRecords(ATTACK)
        given(
            writeReportRepository.writeReport(any())
        ).willReturn(ReportWriteResultDomainModel.Written("content://r"))

        classUnderTest.executeInBackground(
            CreateReportRequestDomainModel(THREE_MONTHS, includeNotes = false)
        )

        val report = argumentCaptor<ReportDomainModel>()
        verify(writeReportRepository).writeReport(report.capture())
        assertEquals(emptyList<Any>(), report.firstValue.notes)
    }

    @Test
    fun `Given nothing logged, a failed read or a failed write then why`() {
        givenRecords()
        assertEquals(
            CreateReportResultDomainModel.NothingLogged,
            classUnderTest.executeInBackground(CreateReportRequestDomainModel(THREE_MONTHS, true))
        )
        verify(writeReportRepository, never()).writeReport(any())

        givenRecords(ATTACK)
        given(
            writeReportRepository.writeReport(any())
        ).willReturn(ReportWriteResultDomainModel.StorageFull)
        assertEquals(
            CreateReportResultDomainModel.StorageFull,
            classUnderTest.executeInBackground(CreateReportRequestDomainModel(THREE_MONTHS, true))
        )

        given(
            getReportRecordsRepository.reportRecords()
        ).willReturn(ReportRecordsResultDomainModel.Failed)
        assertEquals(
            CreateReportResultDomainModel.Failed,
            classUnderTest.executeInBackground(CreateReportRequestDomainModel(THREE_MONTHS, true))
        )
    }

    @Test
    fun `When asking for the options then the dates, and whether anything was logged`() {
        givenRecords(ATTACK)
        val options =
            GetReportOptionsUseCase(
                getCurrentTimeRepository,
                getReportRecordsRepository,
                CoroutineContextProvider.Default
            )

        assertEquals(
            ReportOptionsDomainModel.Ready(
                com.migrely.app.report.domain.model.ReportDatesDomainModel(
                    LocalDate.of(2026, 9, 26),
                    LocalDate.of(2026, 10, 25)
                ),
                anythingLogged = true
            ),
            options.executeInBackground(ReportPeriodDomainModel.LastMonths(1))
        )
        given(
            getReportRecordsRepository.reportRecords()
        ).willReturn(ReportRecordsResultDomainModel.Failed)
        assertEquals(
            ReportOptionsDomainModel.Failed,
            options.executeInBackground(ReportPeriodDomainModel.LastMonths(1))
        )
    }
}
