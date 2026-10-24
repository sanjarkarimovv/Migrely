package com.migrely.app.history.domain.usecase

import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.history.domain.model.AttacksTouchingResultDomainModel
import com.migrely.app.history.domain.model.CurrentTimeDomainModel
import com.migrely.app.history.domain.model.DayMarkDomainModel
import com.migrely.app.history.domain.model.DayMarksResultDomainModel
import com.migrely.app.history.domain.model.DeletedAttackDomainModel
import com.migrely.app.history.domain.model.EarliestAttackResultDomainModel
import com.migrely.app.history.domain.model.HistoryAttackDomainModel
import com.migrely.app.history.domain.model.HistoryMonthDomainModel
import com.migrely.app.history.domain.model.HistoryMonthResultDomainModel
import com.migrely.app.history.domain.repository.GetAttacksTouchingRepository
import com.migrely.app.history.domain.repository.GetCurrentTimeRepository
import com.migrely.app.history.domain.repository.GetDayMarksRepository
import com.migrely.app.history.domain.repository.GetEarliestAttackRepository
import com.migrely.app.history.domain.repository.GetRecentlyDeletedAttackRepository
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

private val TASHKENT: ZoneId = ZoneId.of("Asia/Tashkent")
private val TOKYO: ZoneId = ZoneId.of("Asia/Tokyo")
private val NEW_YORK: ZoneId = ZoneId.of("America/New_York")
private val OCTOBER: YearMonth = YearMonth.of(2026, 10)
private val TODAY: LocalDate = LocalDate.parse("2026-10-23")

class GetHistoryMonthUseCaseTest {
    private lateinit var classUnderTest: GetHistoryMonthUseCase
    private val getCurrentTimeRepository: GetCurrentTimeRepository = mock()
    private val getEarliestAttackRepository: GetEarliestAttackRepository = mock()
    private val getAttacksTouchingRepository: GetAttacksTouchingRepository = mock()
    private val getRecentlyDeletedAttackRepository: GetRecentlyDeletedAttackRepository = mock()
    private val getDayMarksRepository: GetDayMarksRepository = mock()

    @Before
    fun setUp() {
        classUnderTest = GetHistoryMonthUseCase(
            getCurrentTimeRepository,
            getEarliestAttackRepository,
            getAttacksTouchingRepository,
            getRecentlyDeletedAttackRepository,
            getDayMarksRepository,
            CoroutineContextProvider.Default
        )
        givenNow("2026-10-23T12:00")
        givenEarliest(EarliestAttackResultDomainModel.None)
        givenAttacks()
        givenMarks()
    }

    private fun givenMarks(vararg marks: DayMarkDomainModel) {
        given(getDayMarksRepository.dayMarks(any(), any()))
            .willReturn(DayMarksResultDomainModel.Loaded(marks.toList()))
    }

    private fun at(local: String, zone: ZoneId = TASHKENT): Instant =
        LocalDateTime.parse(local).atZone(zone).toInstant()

    private fun givenNow(local: String, zone: ZoneId = TASHKENT) {
        given(getCurrentTimeRepository.currentTime())
            .willReturn(CurrentTimeDomainModel(at(local, zone), zone))
    }

    private fun givenEarliest(result: EarliestAttackResultDomainModel) {
        given(getEarliestAttackRepository.earliestAttack()).willReturn(result)
    }

    private fun givenAttacks(vararg attacks: HistoryAttackDomainModel) {
        given(getAttacksTouchingRepository.attacksTouching(any(), any()))
            .willReturn(AttacksTouchingResultDomainModel.Loaded(attacks.toList()))
    }

    private fun attack(
        id: String,
        start: String,
        end: String?,
        peak: Int? = 5,
        zone: ZoneId = TASHKENT
    ) = HistoryAttackDomainModel(
        id = id,
        start = at(start, zone),
        startZone = zone,
        end = end?.let { at(it, zone) },
        peakIntensity = peak
    )

    private fun loaded(request: YearMonth?): HistoryMonthDomainModel {
        val actual = classUnderTest.executeInBackground(request)
        return (actual as HistoryMonthResultDomainModel.Loaded).month
    }

    private fun HistoryMonthDomainModel.idsOn(dayOfMonth: Int) =
        days[dayOfMonth - 1].attacks.map(HistoryAttackDomainModel::id)

    @Test
    fun `Given no month asked for when executed then the current month with every day`() {
        val actual = loaded(request = null)

        assertEquals(OCTOBER, actual.month)
        assertEquals(TODAY, actual.today)
        assertEquals(31, actual.days.size)
        assertEquals(LocalDate.parse("2026-10-01"), actual.days.first().date)
        assertEquals(LocalDate.parse("2026-10-31"), actual.days.last().date)
    }

    @Test
    fun `Given no attacks when a past month is asked for then the current month`() {
        val actual = loaded(YearMonth.of(2026, 8))

        assertEquals(OCTOBER, actual.month)
        assertEquals(OCTOBER, actual.earliestMonth)
        assertEquals(false, actual.anyAttackLogged)
    }

    @Test
    fun `Given the first attack in August when July is asked for then August`() {
        givenEarliest(EarliestAttackResultDomainModel.Found(at("2026-08-14T10:00"), TASHKENT))

        val actual = loaded(YearMonth.of(2026, 7))

        assertEquals(YearMonth.of(2026, 8), actual.month)
        assertEquals(YearMonth.of(2026, 8), actual.earliestMonth)
        assertEquals(true, actual.anyAttackLogged)
    }

    @Test
    fun `Given the first attack in August when September is asked for then September`() {
        givenEarliest(EarliestAttackResultDomainModel.Found(at("2026-08-14T10:00"), TASHKENT))

        assertEquals(YearMonth.of(2026, 9), loaded(YearMonth.of(2026, 9)).month)
    }

    @Test
    fun `Given a future month when executed then the current month`() {
        assertEquals(OCTOBER, loaded(YearMonth.of(2026, 12)).month)
    }

    @Test
    fun `Given the first attack is on the 1st in Tokyo when executed then capped at this month`() {
        // 1 November 06:00 in Tokyo is still 31 October in Tashkent.
        givenNow("2026-10-31T23:30")
        givenEarliest(EarliestAttackResultDomainModel.Found(at("2026-11-01T06:00", TOKYO), TOKYO))

        val actual = loaded(request = null)

        assertEquals(OCTOBER, actual.earliestMonth)
    }

    @Test
    fun `When executed then reads the month with two days on each side in the device zone`() {
        loaded(request = null)

        verify(getAttacksTouchingRepository).attacksTouching(
            at("2026-09-29T00:00"),
            at("2026-11-03T00:00")
        )
    }

    @Test
    fun `Given an attack past midnight when executed then both days hold it`() {
        givenAttacks(attack("night", "2026-10-14T22:00", "2026-10-15T03:00"))

        val actual = loaded(request = null)

        assertEquals(listOf("night"), actual.idsOn(14))
        assertEquals(listOf("night"), actual.idsOn(15))
        assertTrue(actual.idsOn(16).isEmpty())
    }

    @Test
    fun `Given an attack ending at midnight when executed then the next day stays empty`() {
        givenAttacks(attack("evening", "2026-10-14T21:00", "2026-10-15T00:00"))

        val actual = loaded(request = null)

        assertEquals(listOf("evening"), actual.idsOn(14))
        assertTrue(actual.idsOn(15).isEmpty())
    }

    @Test
    fun `Given an attack logged in Tokyo when the phone is in New York then its Tokyo day`() {
        givenNow("2026-10-23T08:00", NEW_YORK)
        givenAttacks(attack("tokyo", "2026-10-15T08:00", "2026-10-15T09:00", zone = TOKYO))

        val actual = loaded(request = null)

        assertEquals(listOf("tokyo"), actual.idsOn(15))
        assertTrue(actual.idsOn(14).isEmpty())
    }

    @Test
    fun `Given an attack in progress since yesterday when executed then it runs to today`() {
        givenAttacks(attack("open", "2026-10-22T20:00", end = null))

        val actual = loaded(request = null)

        assertEquals(listOf("open"), actual.idsOn(22))
        assertEquals(listOf("open"), actual.idsOn(23))
        assertTrue(actual.idsOn(24).isEmpty())
    }

    @Test
    fun `Given an attack in progress since September when September is shown then its last days`() {
        givenEarliest(EarliestAttackResultDomainModel.Found(at("2026-09-28T20:00"), TASHKENT))
        givenAttacks(attack("open", "2026-09-28T20:00", end = null))

        val actual = loaded(YearMonth.of(2026, 9))

        assertTrue(actual.idsOn(27).isEmpty())
        assertEquals((28..30).map { listOf("open") }, (28..30).map { actual.idsOn(it) })
    }

    @Test
    fun `Given an attack in progress since September when October is shown then up to today`() {
        givenEarliest(EarliestAttackResultDomainModel.Found(at("2026-09-28T20:00"), TASHKENT))
        givenAttacks(attack("open", "2026-09-28T20:00", end = null))

        val actual = loaded(request = null)

        assertEquals((1..23).map { listOf("open") }, (1..23).map { actual.idsOn(it) })
        assertTrue(actual.idsOn(24).isEmpty())
    }

    @Test
    fun `Given an attack from the last day of September when October is shown then it is listed`() {
        givenAttacks(attack("across", "2026-09-30T23:00", "2026-10-01T02:00"))

        val actual = loaded(request = null)

        assertEquals(listOf("across"), actual.idsOn(1))
        assertEquals(listOf("across"), actual.attacks.map(HistoryAttackDomainModel::id))
    }

    @Test
    fun `Given an attack inside the margin only when executed then it is left out`() {
        givenAttacks(attack("september", "2026-09-29T10:00", "2026-09-29T12:00"))

        val actual = loaded(request = null)

        assertTrue(actual.attacks.isEmpty())
        assertTrue(actual.days.all { it.attacks.isEmpty() })
    }

    @Test
    fun `Given several attacks when executed then days oldest first and the month newest first`() {
        givenAttacks(
            attack("morning", "2026-10-14T09:10", "2026-10-14T10:00"),
            attack("evening", "2026-10-14T18:00", "2026-10-14T19:00"),
            attack("early", "2026-10-03T08:00", "2026-10-03T09:00"),
            attack("late", "2026-10-20T08:00", "2026-10-20T09:00")
        )

        val actual = loaded(request = null)

        assertEquals(listOf("morning", "evening"), actual.idsOn(14))
        assertEquals(
            listOf("late", "evening", "morning", "early"),
            actual.attacks.map(HistoryAttackDomainModel::id)
        )
    }

    @Test
    fun `Given a recent deletion when executed then it is reported`() {
        val deleted = DeletedAttackDomainModel("gone", at("2026-10-23T11:59"))
        given(getRecentlyDeletedAttackRepository.recentlyDeletedAttack()).willReturn(deleted)

        val actual = classUnderTest.executeInBackground(null)

        assertEquals(deleted, (actual as HistoryMonthResultDomainModel.Loaded).recentlyDeleted)
    }

    @Test
    fun `Given the earliest attack cannot be read when executed then failed`() {
        givenEarliest(EarliestAttackResultDomainModel.Failed)

        assertEquals(HistoryMonthResultDomainModel.Failed, classUnderTest.executeInBackground(null))
    }

    @Test
    fun `Given the attacks cannot be read when executed then failed`() {
        given(getAttacksTouchingRepository.attacksTouching(any(), any()))
            .willReturn(AttacksTouchingResultDomainModel.Failed)

        assertEquals(HistoryMonthResultDomainModel.Failed, classUnderTest.executeInBackground(null))
    }

    @Test
    fun `When executed then reads the marks of the month's first to last day`() {
        loaded(OCTOBER)

        verify(getDayMarksRepository).dayMarks(
            LocalDate.parse("2026-10-01"),
            LocalDate.parse("2026-10-31")
        )
    }

    @Test
    fun `Given a period mark on a storm day when executed then the day keeps both`() {
        givenAttacks(attack("a", "2026-10-14T09:00", "2026-10-14T12:00", peak = 9))
        givenMarks(
            DayMarkDomainModel(LocalDate.parse("2026-10-14"), period = true, painFree = false)
        )

        val day = loaded(OCTOBER).days[13]

        assertEquals(listOf("a"), day.attacks.map(HistoryAttackDomainModel::id))
        assertEquals(true to false, day.period to day.painFree)
    }

    @Test
    fun `Given a pain-free mark on a day without attacks when executed then pain-free`() {
        givenMarks(
            DayMarkDomainModel(LocalDate.parse("2026-10-06"), period = false, painFree = true)
        )

        assertTrue(loaded(OCTOBER).days[5].painFree)
    }

    @Test
    fun `Given a pain-free mark on a day an attack touches when executed then not pain-free`() {
        // The attack wins, and the stored mark is kept for when the attack is deleted (BRD EC-DAY-01).
        givenAttacks(attack("a", "2026-10-05T23:30", "2026-10-06T01:00"))
        givenMarks(
            DayMarkDomainModel(LocalDate.parse("2026-10-06"), period = false, painFree = true)
        )

        assertEquals(false, loaded(OCTOBER).days[5].painFree)
    }

    @Test
    fun `Given the marks cannot be read when executed then failed`() {
        given(
            getDayMarksRepository.dayMarks(any(), any())
        ).willReturn(DayMarksResultDomainModel.Failed)

        assertEquals(HistoryMonthResultDomainModel.Failed, classUnderTest.executeInBackground(null))
    }
}
