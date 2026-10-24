package com.migrely.app.medication.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.medication.domain.model.AcuteIntakeDomainModel
import com.migrely.app.medication.domain.model.AcuteIntakesResultDomainModel
import com.migrely.app.medication.domain.model.CurrentTimeDomainModel
import com.migrely.app.medication.domain.model.MedicationCategoryDomainModel
import com.migrely.app.medication.domain.model.OveruseCategoryDomainModel
import com.migrely.app.medication.domain.model.OveruseStatusResultDomainModel
import com.migrely.app.medication.domain.model.OveruseStatusRowDomainModel
import com.migrely.app.medication.domain.repository.GetAcuteIntakesRepository
import com.migrely.app.medication.domain.repository.GetCurrentTimeRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

private val TASHKENT = ZoneId.of("Asia/Tashkent")
private val TODAY = LocalDate.of(2026, 10, 24)
private val NOW = TODAY.atTime(15, 0).atZone(TASHKENT).toInstant()

class GetOveruseStatusUseCaseTest {
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val getAcuteIntakesRepository: GetAcuteIntakesRepository = mock()
    private lateinit var classUnderTest: GetOveruseStatusUseCase

    @Before
    fun setUp() {
        classUnderTest = GetOveruseStatusUseCase(
            getCurrentTimeRepository,
            getAcuteIntakesRepository,
            CoroutineContextProvider.Default
        )
        given(
            getCurrentTimeRepository.currentTime()
        ).willReturn(CurrentTimeDomainModel(NOW, TASHKENT))
    }

    private fun intake(daysAgo: Long, category: MedicationCategoryDomainModel?) =
        AcuteIntakeDomainModel(
            TODAY.minusDays(daysAgo).atTime(12, 0).atZone(TASHKENT).toInstant(),
            TASHKENT,
            category
        )

    private fun givenIntakes(vararg intakes: AcuteIntakeDomainModel) {
        given(getAcuteIntakesRepository.acuteIntakesTakenBetween(any(), any()))
            .willReturn(AcuteIntakesResultDomainModel.Loaded(intakes.toList()))
    }

    private fun rows() =
        (classUnderTest.executeInBackground(Unit) as OveruseStatusResultDomainModel.Loaded).rows

    @Test
    fun `Given one category when executed then its days against its limit`() {
        givenIntakes(
            intake(0, MedicationCategoryDomainModel.SimplePainkiller),
            intake(0, MedicationCategoryDomainModel.SimplePainkiller),
            intake(3, MedicationCategoryDomainModel.SimplePainkiller),
            // Day 30 is outside the window of today and the 29 days before it.
            intake(30, MedicationCategoryDomainModel.SimplePainkiller)
        )

        assertEquals(
            listOf(OveruseStatusRowDomainModel(OveruseCategoryDomainModel.SimplePainkiller, 2, 15)),
            rows()
        )
    }

    @Test
    fun `Given two categories when executed then each, then all attack medication`() {
        givenIntakes(
            intake(1, MedicationCategoryDomainModel.Triptan),
            intake(2, MedicationCategoryDomainModel.SimplePainkiller),
            intake(2, null)
        )

        assertEquals(
            listOf(
                OveruseStatusRowDomainModel(OveruseCategoryDomainModel.SimplePainkiller, 1, 15),
                OveruseStatusRowDomainModel(OveruseCategoryDomainModel.Triptan, 1, 10),
                OveruseStatusRowDomainModel(OveruseCategoryDomainModel.CombinationOrOther, 1, 10),
                OveruseStatusRowDomainModel(OveruseCategoryDomainModel.Multiple, 2, 10)
            ),
            rows()
        )
    }

    @Test
    fun `Given no intakes when executed then no rows`() {
        givenIntakes()

        assertEquals(emptyList<OveruseStatusRowDomainModel>(), rows())
    }

    @Test
    fun `Given the intakes cannot be read when executed then failed`() {
        given(getAcuteIntakesRepository.acuteIntakesTakenBetween(any(), any()))
            .willReturn(AcuteIntakesResultDomainModel.Failed)

        assertEquals(
            OveruseStatusResultDomainModel.Failed,
            classUnderTest.executeInBackground(Unit)
        )
    }

    @Test
    fun `When executed then reads the 30 days with two days of zone margin`() {
        givenIntakes()

        classUnderTest.executeInBackground(Unit)

        // 23 September 00:00 to 27 October 00:00 in Tashkent (UTC+5).
        verify(getAcuteIntakesRepository).acuteIntakesTakenBetween(
            Instant.parse("2026-09-22T19:00:00Z"),
            Instant.parse("2026-10-26T19:00:00Z")
        )
    }
}
