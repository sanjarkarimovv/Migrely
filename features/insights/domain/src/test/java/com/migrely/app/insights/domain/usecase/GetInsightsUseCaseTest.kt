package com.migrely.app.insights.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.insights.domain.model.CurrentTimeDomainModel
import com.migrely.app.insights.domain.model.InsightsAttackDomainModel
import com.migrely.app.insights.domain.model.InsightsBuiltInTriggerDomainModel
import com.migrely.app.insights.domain.model.InsightsDatesDomainModel
import com.migrely.app.insights.domain.model.InsightsPatternsDomainModel
import com.migrely.app.insights.domain.model.InsightsRecordsDomainModel
import com.migrely.app.insights.domain.model.InsightsRecordsResultDomainModel
import com.migrely.app.insights.domain.model.InsightsResultDomainModel
import com.migrely.app.insights.domain.model.InsightsTriggerDomainModel
import com.migrely.app.insights.domain.repository.GetCurrentTimeRepository
import com.migrely.app.insights.domain.repository.GetInsightsRecordsRepository
import com.migrely.app.insights.domain.repository.GetPeriodTrackingRepository
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

private val ZONE: ZoneId = ZoneId.of("Asia/Tashkent")

// 25 October in Tashkent, still 24 October in UTC.
private val NOW: Instant = LocalDateTime.parse("2026-10-25T02:00").atZone(ZONE).toInstant()

private val STRESS = InsightsTriggerDomainModel.BuiltIn(InsightsBuiltInTriggerDomainModel.Stress)

private fun attack(start: String) = LocalDateTime.parse(start).atZone(ZONE).toInstant().let {
    InsightsAttackDomainModel(it, ZONE, it.plusSeconds(7_200), null, listOf(STRESS))
}

private val SIX = listOf(
    attack("2026-06-01T09:00"),
    attack("2026-07-01T09:00"),
    attack("2026-08-01T09:00"),
    attack("2026-09-01T09:00"),
    attack("2026-10-01T09:00"),
    attack("2026-10-20T09:00")
)
private val PERIODS = listOf("2026-08-10", "2026-09-08", "2026-10-06").map(LocalDate::parse).toSet()

class GetInsightsUseCaseTest {
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock {
        on { currentTime() } doReturn CurrentTimeDomainModel(NOW, ZONE)
    }
    private val getInsightsRecordsRepository: GetInsightsRecordsRepository = mock()
    private val getPeriodTrackingRepository: GetPeriodTrackingRepository = mock()
    private val classUnderTest = GetInsightsUseCase(
        getCurrentTimeRepository,
        getInsightsRecordsRepository,
        getPeriodTrackingRepository,
        CoroutineContextProvider.Default
    )

    private fun givenRecords(attacks: List<InsightsAttackDomainModel>) =
        given(getInsightsRecordsRepository.insightsRecords()).willReturn(
            InsightsRecordsResultDomainModel.Loaded(
                InsightsRecordsDomainModel(attacks, emptyList(), PERIODS)
            )
        )

    private fun insightsOf(months: Int) =
        (classUnderTest.executeInBackground(months) as InsightsResultDomainModel.Ready).insights

    @Test
    fun `When getting insights then the period ends today in the device's zone`() {
        givenRecords(SIX)

        val insights = insightsOf(6)

        assertEquals(6, insights.months)
        assertEquals(
            InsightsDatesDomainModel(LocalDate.of(2026, 4, 26), LocalDate.of(2026, 10, 25)),
            insights.dates
        )
        assertEquals(6, (insights.patterns as InsightsPatternsDomainModel.Enough).migraines)
        assertEquals(YearMonth.of(2026, 10), insights.trends.last().month)
        assertEquals(12, insights.trends.size)
    }

    @Test
    fun `Given too few migraines in the period then what is missing, and the trends anyway`() {
        givenRecords(SIX)

        val insights = insightsOf(3)

        assertEquals(
            InsightsPatternsDomainModel.NotEnough(missingMigraines = 1, missingDays = 0),
            insights.patterns
        )
        assertEquals(6, insights.trends.sumOf { it.migraines })
    }

    @Test
    fun `The cycle follows Track periods`() {
        givenRecords(SIX)

        given(getPeriodTrackingRepository.periodTracking()).willReturn(true)
        assertNotNull((insightsOf(6).patterns as InsightsPatternsDomainModel.Enough).cycle)

        given(getPeriodTrackingRepository.periodTracking()).willReturn(false)
        assertNull((insightsOf(6).patterns as InsightsPatternsDomainModel.Enough).cycle)
    }

    @Test
    fun `Given the records cannot be read then failed`() {
        given(getInsightsRecordsRepository.insightsRecords())
            .willReturn(InsightsRecordsResultDomainModel.Failed)

        assertEquals(InsightsResultDomainModel.Failed, classUnderTest.executeInBackground(6))
    }
}
