package com.migrely.app.medication.domain.usecase

import com.migrely.app.medication.domain.model.AcuteIntakeDomainModel
import com.migrely.app.medication.domain.model.AcuteIntakesResultDomainModel
import com.migrely.app.medication.domain.model.CurrentTimeDomainModel
import com.migrely.app.medication.domain.model.MedicationCategoryDomainModel
import com.migrely.app.medication.domain.model.OveruseCategoryDomainModel
import com.migrely.app.medication.domain.repository.GetAcuteIntakesRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

private val TASHKENT = ZoneId.of("Asia/Tashkent")
private val BERLIN = ZoneId.of("Europe/Berlin")
private val TOKYO = ZoneId.of("Asia/Tokyo")
private val TODAY = LocalDate.of(2026, 10, 24)
private val NOW = CurrentTimeDomainModel(TODAY.atTime(15, 0).atZone(TASHKENT).toInstant(), TASHKENT)

class OveruseDayCounterTest {
    private val getAcuteIntakesRepository: GetAcuteIntakesRepository = mock()
    private val classUnderTest = OveruseDayCounter(getAcuteIntakesRepository)

    private fun intake(local: String, zone: ZoneId = TASHKENT) = AcuteIntakeDomainModel(
        LocalDateTime.parse(local).atZone(zone).toInstant(),
        zone,
        MedicationCategoryDomainModel.Triptan
    )

    private fun givenIntakes(vararg intakes: AcuteIntakeDomainModel) {
        given(getAcuteIntakesRepository.acuteIntakesTakenBetween(any(), any()))
            .willReturn(AcuteIntakesResultDomainModel.Loaded(intakes.toList()))
    }

    @Test
    fun `Given the window's first and last days then both count, and the day before not`() {
        givenIntakes(
            // 25 September is the 29th day before 24 October; 24 September is outside.
            intake("2026-09-25T08:00"),
            intake("2026-10-24T08:00"),
            intake("2026-09-24T22:00")
        )

        assertEquals(mapOf(OveruseCategoryDomainModel.Triptan to 2), classUnderTest.days(NOW))
    }

    @Test
    fun `Given intakes in other zones near the edge when counted then on their own dates`() {
        givenIntakes(
            // 23:30 on 24 September in Berlin: its own date is outside the window.
            intake("2026-09-24T23:30", BERLIN),
            // 00:30 on 25 October in Tokyo: its own date is after today.
            intake("2026-10-25T00:30", TOKYO),
            // 20:00 on 24 October in Berlin: today.
            intake("2026-10-24T20:00", BERLIN)
        )

        assertEquals(mapOf(OveruseCategoryDomainModel.Triptan to 1), classUnderTest.days(NOW))
    }

    @Test
    fun `Given the intakes cannot be read when counted then null`() {
        given(getAcuteIntakesRepository.acuteIntakesTakenBetween(any(), any()))
            .willReturn(AcuteIntakesResultDomainModel.Failed)

        assertNull(classUnderTest.days(NOW))
    }
}
