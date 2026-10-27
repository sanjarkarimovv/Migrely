package com.migrely.app.history.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.history.domain.model.AttacksTouchingResultDomainModel
import com.migrely.app.history.domain.model.CurrentTimeDomainModel
import com.migrely.app.history.domain.model.DayMarkKindDomainModel
import com.migrely.app.history.domain.model.DayMarkWriteResultDomainModel
import com.migrely.app.history.domain.model.HistoryAttackDomainModel
import com.migrely.app.history.domain.model.SetDayMarkRequestDomainModel
import com.migrely.app.history.domain.model.SetDayMarkResultDomainModel
import com.migrely.app.history.domain.repository.GetAttacksTouchingRepository
import com.migrely.app.history.domain.repository.GetCurrentTimeRepository
import com.migrely.app.history.domain.repository.SetDayMarkRepository
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

private val TASHKENT: ZoneId = ZoneId.of("Asia/Tashkent")
private val TOKYO: ZoneId = ZoneId.of("Asia/Tokyo")
private val OCTOBER_6: LocalDate = LocalDate.parse("2026-10-06")

class SetDayMarkUseCaseTest {
    private lateinit var classUnderTest: SetDayMarkUseCase
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val getAttacksTouchingRepository: GetAttacksTouchingRepository = mock()
    private val setDayMarkRepository: SetDayMarkRepository = mock()

    @Before
    fun setUp() {
        given(getCurrentTimeRepository.currentTime())
            .willReturn(CurrentTimeDomainModel(at("2026-10-23T12:00"), TASHKENT))
        givenAttacks()
        given(setDayMarkRepository.setDayMark(any(), any(), any()))
            .willReturn(DayMarkWriteResultDomainModel.Written)
        classUnderTest = SetDayMarkUseCase(
            getCurrentTimeRepository,
            getAttacksTouchingRepository,
            setDayMarkRepository,
            CoroutineContextProvider.Default
        )
    }

    @Test
    fun `Given a past day without attacks when marking pain-free then done`() {
        assertEquals(SetDayMarkResultDomainModel.Done, execute(OCTOBER_6, painFree = true))
        verify(setDayMarkRepository).setDayMark(OCTOBER_6, DayMarkKindDomainModel.PainFree, true)
    }

    @Test
    fun `Given an attack on the day when marking pain-free then refused`() {
        givenAttacks(attack("2026-10-06T09:00", "2026-10-06T11:00"))

        assertEquals(SetDayMarkResultDomainModel.AttackLogged, execute(OCTOBER_6, painFree = true))
        verify(setDayMarkRepository, never()).setDayMark(any(), any(), any())
    }

    @Test
    fun `Given an attack from the night before when marking the next day pain-free then refused`() {
        givenAttacks(attack("2026-10-05T23:00", "2026-10-06T02:00"))

        assertEquals(SetDayMarkResultDomainModel.AttackLogged, execute(OCTOBER_6, painFree = true))
    }

    @Test
    fun `Given an attack ending at midnight when marking the next day pain-free then done`() {
        givenAttacks(attack("2026-10-05T22:00", "2026-10-06T00:00"))

        assertEquals(SetDayMarkResultDomainModel.Done, execute(OCTOBER_6, painFree = true))
    }

    @Test
    fun `Given an attack logged in Tokyo on its own 7 October when marking 6 October then done`() {
        // 01:00 on 7 October in Tokyo is 21:00 on 6 October in Tashkent, but the attack belongs to 7 October.
        givenAttacks(attack("2026-10-07T01:00", "2026-10-07T02:00", TOKYO))

        assertEquals(SetDayMarkResultDomainModel.Done, execute(OCTOBER_6, painFree = true))
    }

    @Test
    fun `Given an attack on the day when marking a period then done`() {
        givenAttacks(attack("2026-10-06T09:00", "2026-10-06T11:00"))

        assertEquals(SetDayMarkResultDomainModel.Done, execute(OCTOBER_6, painFree = false))
        verify(setDayMarkRepository).setDayMark(OCTOBER_6, DayMarkKindDomainModel.Period, true)
    }

    @Test
    fun `Given an attack on the day when removing a pain-free mark then done`() {
        givenAttacks(attack("2026-10-06T09:00", "2026-10-06T11:00"))

        assertEquals(
            SetDayMarkResultDomainModel.Done,
            execute(OCTOBER_6, painFree = true, marked = false)
        )
    }

    @Test
    fun `Given today when marking then done, and tomorrow then refused`() {
        assertEquals(
            SetDayMarkResultDomainModel.Done,
            execute(LocalDate.parse("2026-10-23"), painFree = false)
        )
        assertEquals(
            SetDayMarkResultDomainModel.FutureDay,
            execute(LocalDate.parse("2026-10-24"), painFree = false)
        )
    }

    @Test
    fun `Given the attacks cannot be read when marking pain-free then failed`() {
        given(getAttacksTouchingRepository.attacksTouching(any(), any()))
            .willReturn(AttacksTouchingResultDomainModel.Failed)

        assertEquals(SetDayMarkResultDomainModel.Failed, execute(OCTOBER_6, painFree = true))
    }

    @Test
    fun `Given storage is full when marking then storage full`() {
        given(setDayMarkRepository.setDayMark(any(), any(), any()))
            .willReturn(DayMarkWriteResultDomainModel.StorageFull)

        assertEquals(SetDayMarkResultDomainModel.StorageFull, execute(OCTOBER_6, painFree = false))
    }

    @Test
    fun `Given the write fails when marking then failed`() {
        given(setDayMarkRepository.setDayMark(any(), any(), any()))
            .willReturn(DayMarkWriteResultDomainModel.Failed)

        assertEquals(SetDayMarkResultDomainModel.Failed, execute(OCTOBER_6, painFree = false))
    }

    private fun execute(date: LocalDate, painFree: Boolean, marked: Boolean = true) =
        classUnderTest.executeInBackground(
            SetDayMarkRequestDomainModel(
                date,
                if (painFree) DayMarkKindDomainModel.PainFree else DayMarkKindDomainModel.Period,
                marked
            )
        )

    private fun at(local: String, zone: ZoneId = TASHKENT): Instant =
        LocalDateTime.parse(local).atZone(zone).toInstant()

    private fun attack(start: String, end: String, zone: ZoneId = TASHKENT) =
        HistoryAttackDomainModel("a", at(start, zone), zone, at(end, zone), peakIntensity = 5)

    private fun givenAttacks(vararg attacks: HistoryAttackDomainModel) {
        given(getAttacksTouchingRepository.attacksTouching(any(), any()))
            .willReturn(AttacksTouchingResultDomainModel.Loaded(attacks.toList()))
    }

    @Test
    fun `Given an attack in progress today when marking today pain-free then refused`() {
        givenAttacks(
            HistoryAttackDomainModel(
                "a",
                at("2026-10-23T09:00"),
                TASHKENT,
                end = null,
                peakIntensity = null
            )
        )

        assertEquals(
            SetDayMarkResultDomainModel.AttackLogged,
            execute(LocalDate.parse("2026-10-23"), painFree = true)
        )
    }

    @Test
    fun `When marking pain-free then reads two days around the date in the device zone`() {
        execute(OCTOBER_6, painFree = true)

        // From 4 October 00:00 to 9 October 00:00 in Tashkent (UTC+5).
        verify(getAttacksTouchingRepository).attacksTouching(
            Instant.parse("2026-10-03T19:00:00Z"),
            Instant.parse("2026-10-08T19:00:00Z")
        )
    }

    @Test
    fun `Given a future day when marking then nothing is written`() {
        execute(LocalDate.parse("2026-10-24"), painFree = true)

        verify(setDayMarkRepository, never()).setDayMark(any(), any(), any())
    }

    @Test
    fun `Given a future day when removing a mark then done`() {
        assertEquals(
            SetDayMarkResultDomainModel.Done,
            execute(LocalDate.parse("2026-10-25"), painFree = false, marked = false)
        )
    }
}
