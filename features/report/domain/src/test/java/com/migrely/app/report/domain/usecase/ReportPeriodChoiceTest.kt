package com.migrely.app.report.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.report.domain.model.CreateReportRequestDomainModel
import com.migrely.app.report.domain.model.CreateReportResultDomainModel
import com.migrely.app.report.domain.model.CurrentTimeDomainModel
import com.migrely.app.report.domain.model.ReportDatesDomainModel
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

private val ZONE: ZoneId = ZoneId.of("Asia/Tashkent")
private val NOVEMBER_18: LocalDate = LocalDate.of(2026, 11, 18)
private val AUGUST_20: LocalDate = LocalDate.of(2026, 8, 20)

/** "Since last visit" on the Doctor report (BRD EC-REP-04, D168, design D4). */
class ReportPeriodChoiceTest {
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val getReportRecordsRepository: GetReportRecordsRepository = mock()
    private val writeReportRepository: WriteReportRepository = mock {
        on { writeReport(any()) } doReturn ReportWriteResultDomainModel.Written("content://r")
    }
    private val options = GetReportOptionsUseCase(
        getCurrentTimeRepository,
        getReportRecordsRepository,
        CoroutineContextProvider.Default
    )
    private val create = CreateReportUseCase(
        getCurrentTimeRepository,
        getReportRecordsRepository,
        writeReportRepository,
        mock<CleanUpReportsRepository>(),
        CoroutineContextProvider.Default
    )

    private fun givenToday(date: LocalDate, vararg visits: LocalDate) {
        given(getCurrentTimeRepository.currentTime()).willReturn(
            CurrentTimeDomainModel(
                LocalDateTime.of(
                    date.year,
                    date.month,
                    date.dayOfMonth,
                    12,
                    0
                ).atZone(ZONE).toInstant(),
                ZONE
            )
        )
        given(getReportRecordsRepository.reportRecords()).willReturn(
            ReportRecordsResultDomainModel.Loaded(
                ReportRecordsDomainModel(emptyList(), emptyList(), setOf(AUGUST_20), visits.toSet())
            )
        )
    }

    private fun ready(period: ReportPeriodDomainModel) =
        options.executeInBackground(period) as ReportOptionsDomainModel.Ready

    @Test
    fun `Given a visit on August 20 when opened on November 18 then since it is chosen`() {
        givenToday(NOVEMBER_18, AUGUST_20)

        val ready = ready(ReportPeriodDomainModel.Default)

        assertEquals(ReportPeriodDomainModel.SinceLastVisit(AUGUST_20), ready.period)
        assertEquals(AUGUST_20, ready.lastVisit)
        assertEquals(ReportDatesDomainModel(AUGUST_20, NOVEMBER_18), ready.dates)
    }

    @Test
    fun `Given no visit when the report opens then 3 months and no chip (control)`() {
        givenToday(NOVEMBER_18)

        val ready = ready(ReportPeriodDomainModel.Default)

        assertEquals(ReportPeriodDomainModel.LastMonths(3), ready.period)
        assertEquals(null, ready.lastVisit)
    }

    @Test
    fun `Given visits August 20 and November 20 when opened November 20 then since August 20`() {
        val november20 = LocalDate.of(2026, 11, 20)
        givenToday(november20, AUGUST_20, november20)

        val ready = ready(ReportPeriodDomainModel.Default)

        assertEquals(ReportPeriodDomainModel.SinceLastVisit(AUGUST_20), ready.period)
        assertEquals(ReportDatesDomainModel(AUGUST_20, november20), ready.dates)
    }

    @Test
    fun `Given a last visit over 24 months ago when the report opens then 3 months, no chip`() {
        givenToday(NOVEMBER_18, NOVEMBER_18.minusMonths(24).minusDays(1))

        val ready = ready(ReportPeriodDomainModel.Default)

        assertEquals(ReportPeriodDomainModel.LastMonths(3), ready.period)
        assertEquals(null, ready.lastVisit)
    }

    @Test
    fun `Given 6 months chosen when the options are read then 6 months, chip still offered`() {
        givenToday(NOVEMBER_18, AUGUST_20)

        val ready = ready(ReportPeriodDomainModel.LastMonths(6))

        assertEquals(ReportPeriodDomainModel.LastMonths(6), ready.period)
        assertEquals(AUGUST_20, ready.lastVisit)
    }

    @Test
    fun `Given the default period when the report is made then it covers since the last visit`() {
        givenToday(NOVEMBER_18, AUGUST_20)

        assertEquals(
            CreateReportResultDomainModel.Created("content://r"),
            create.executeInBackground(
                CreateReportRequestDomainModel(ReportPeriodDomainModel.Default, false)
            )
        )

        val report = argumentCaptor<ReportDomainModel>()
        verify(writeReportRepository).writeReport(report.capture())
        assertEquals(ReportDatesDomainModel(AUGUST_20, NOVEMBER_18), report.firstValue.dates)
    }

    @Test
    fun `Given a visit on November 9 and a later one when opened November 10 then since November 9`() {
        val november9 = LocalDate.of(2026, 11, 9)
        val november10 = LocalDate.of(2026, 11, 10)
        givenToday(november10, november9, LocalDate.of(2026, 11, 27))

        val ready = ready(ReportPeriodDomainModel.Default)

        assertEquals(ReportPeriodDomainModel.SinceLastVisit(november9), ready.period)
        assertEquals(ReportDatesDomainModel(november9, november10), ready.dates)
    }
}
