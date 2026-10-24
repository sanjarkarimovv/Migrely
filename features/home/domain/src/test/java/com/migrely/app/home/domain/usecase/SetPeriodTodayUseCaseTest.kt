package com.migrely.app.home.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.model.CurrentTimeDomainModel
import com.migrely.app.home.domain.model.WriteResultDomainModel
import com.migrely.app.home.domain.repository.GetCurrentTimeRepository
import com.migrely.app.home.domain.repository.SetPeriodRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.mock

class SetPeriodTodayUseCaseTest {
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val setPeriodRepository: SetPeriodRepository = mock()
    private val classUnderTest = SetPeriodTodayUseCase(
        getCurrentTimeRepository,
        setPeriodRepository,
        CoroutineContextProvider.Default
    )

    @Test
    fun `When marking then marks the device's date`() {
        // 01:30 on 24 October in Tashkent is still 23 October in UTC.
        given(getCurrentTimeRepository.currentTime()).willReturn(
            CurrentTimeDomainModel(
                Instant.parse("2026-10-23T20:30:00Z"),
                ZoneId.of("Asia/Tashkent")
            )
        )
        given(setPeriodRepository.setPeriod(LocalDate.parse("2026-10-24"), true))
            .willReturn(WriteResultDomainModel.Written)

        assertEquals(WriteResultDomainModel.Written, classUnderTest.executeInBackground(true))
    }

    @Test
    fun `Given storage is full when unmarking then storage full`() {
        given(getCurrentTimeRepository.currentTime()).willReturn(
            CurrentTimeDomainModel(
                Instant.parse("2026-10-23T10:00:00Z"),
                ZoneId.of("Asia/Tashkent")
            )
        )
        given(setPeriodRepository.setPeriod(LocalDate.parse("2026-10-23"), false))
            .willReturn(WriteResultDomainModel.StorageFull)

        assertEquals(WriteResultDomainModel.StorageFull, classUnderTest.executeInBackground(false))
    }
}
