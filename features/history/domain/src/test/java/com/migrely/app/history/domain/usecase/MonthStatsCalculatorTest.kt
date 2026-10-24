package com.migrely.app.history.domain.usecase

import com.migrely.app.history.domain.model.AttackMedicationIntakeDomainModel
import com.migrely.app.history.domain.model.DayMarkDomainModel
import com.migrely.app.history.domain.model.HistoryAttackDomainModel
import com.migrely.app.history.domain.model.PainLevelDomainModel
import com.migrely.app.history.domain.model.ProgressDomainModel
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private val TASHKENT = ZoneId.of("Asia/Tashkent")
private val NEW_YORK = ZoneId.of("America/New_York")
private val SEPTEMBER = YearMonth.of(2026, 9)
private val OCTOBER = YearMonth.of(2026, 10)
private val NOW = Instant.parse("2026-10-24T07:00:00Z")
private val MILD = 3
private val MODERATE = 6

class MonthStatsCalculatorTest {
    private val classUnderTest = MonthStatsCalculator()

    private fun at(local: String, zone: ZoneId = TASHKENT): Instant =
        LocalDateTime.parse(local).atZone(zone).toInstant()

    private fun attack(start: String, hours: Long? = 2, peak: Int? = null) =
        HistoryAttackDomainModel(
            id = start,
            start = at(start),
            startZone = TASHKENT,
            end = hours?.let { at(start).plus(Duration.ofHours(it)) },
            peakIntensity = peak
        )

    private fun painFree(date: String) =
        DayMarkDomainModel(LocalDate.parse(date), period = false, painFree = true)

    private fun periodDays(month: YearMonth, days: IntRange) =
        days.map { DayMarkDomainModel(month.atDay(it), period = true, painFree = false) }

    private fun entries(
        attacks: List<HistoryAttackDomainModel> = emptyList(),
        marks: List<DayMarkDomainModel> = emptyList(),
        intakes: List<AttackMedicationIntakeDomainModel> = emptyList()
    ) = PeriodEntries(attacks, marks, intakes)

    private fun september(current: PeriodEntries, previous: PeriodEntries? = null) =
        classUnderTest.calculate(SEPTEMBER, SEPTEMBER.atEndOfMonth(), NOW, current, previous)

    @Test
    fun `Given a past month when counted then its migraines and migraine days`() {
        val actual = september(
            entries(
                attacks = listOf(
                    attack("2026-09-03T10:00"),
                    attack("2026-09-12T10:00"),
                    // 29 September 20:00 to 30 September 08:00.
                    attack("2026-09-29T20:00", hours = 12)
                )
            )
        )

        assertEquals(3, actual.migraines)
        assertEquals(4, actual.migraineDays)
    }

    @Test
    fun `Given an attack from the month before then a migraine day but no migraine`() {
        // 31 August 22:00 to 1 September 03:00.
        val actual = september(entries(attacks = listOf(attack("2026-08-31T22:00", hours = 5))))

        assertEquals(0, actual.migraines)
        assertEquals(1, actual.migraineDays)
    }

    @Test
    fun `Given a pain-free mark under an attack when counted then a migraine day only`() {
        val actual = september(
            entries(
                attacks = listOf(attack("2026-09-10T10:00")),
                marks = listOf(painFree("2026-09-10"), painFree("2026-09-11"))
            )
        )

        assertEquals(1, actual.painFreeDays)
        assertEquals(1, actual.migraineDays)
    }

    @Test
    fun `Given intakes when counted then the days they were taken on in their zones`() {
        val actual = september(
            entries(
                intakes = listOf(
                    AttackMedicationIntakeDomainModel(at("2026-09-04T09:00"), TASHKENT),
                    AttackMedicationIntakeDomainModel(at("2026-09-04T18:00"), TASHKENT),
                    // 30 September in New York, already 1 October in Tashkent.
                    AttackMedicationIntakeDomainModel(at("2026-09-30T20:00", NEW_YORK), NEW_YORK)
                )
            )
        )

        assertEquals(2, actual.attackMedicationDays)
    }

    @Test
    fun `Given a mild and a moderate attack when counted then moderate on average`() {
        val actual = september(
            entries(
                attacks = listOf(
                    attack("2026-09-03T10:00", peak = MILD),
                    attack("2026-09-05T10:00", peak = MODERATE),
                    attack("2026-09-07T10:00", peak = null)
                )
            )
        )

        assertEquals(PainLevelDomainModel.Moderate, actual.averagePeak)
    }

    @Test
    fun `Given two mild and one moderate attack when counted then mild on average`() {
        val actual = september(
            entries(
                attacks = listOf(
                    attack("2026-09-03T10:00", peak = MILD),
                    attack("2026-09-05T10:00", peak = MILD),
                    attack("2026-09-07T10:00", peak = MODERATE)
                )
            )
        )

        assertEquals(PainLevelDomainModel.Mild, actual.averagePeak)
    }

    @Test
    fun `Given no pain level when counted then no average peak`() {
        assertNull(september(entries(attacks = listOf(attack("2026-09-03T10:00")))).averagePeak)
    }

    @Test
    fun `Given 15 or 14 migraine days when counted then chronic or not`() {
        val fifteen = (1..15).map { attack("2026-09-%02dT10:00".format(it)) }

        assertEquals(true, september(entries(attacks = fifteen)).chronic)
        assertEquals(false, september(entries(attacks = fifteen.drop(1))).chronic)
    }

    @Test
    fun `Given a past month with enough entries then compared with the whole month before`() {
        val actual = september(
            current = entries(
                attacks = (1..6).map { attack("2026-09-%02dT10:00".format(it)) },
                marks = periodDays(SEPTEMBER, 20..24)
            ),
            previous = entries(
                attacks = (1..9).map { attack("2026-08-%02dT10:00".format(it)) },
                marks = periodDays(YearMonth.of(2026, 8), 20..30)
            )
        )

        assertEquals(
            ProgressDomainModel(
                -3,
                ProgressDomainModel.ComparedWith.WholeMonth(YearMonth.of(2026, 8))
            ),
            actual.progress
        )
    }

    @Test
    fun `Given the current month when counted then compared with the same dates of last month`() {
        val actual = classUnderTest.calculate(
            month = OCTOBER,
            last = LocalDate.of(2026, 10, 24),
            now = NOW,
            current = entries(
                attacks = listOf(attack("2026-10-02T10:00")) +
                    // In progress since 22 October: 22, 23 and 24 October so far.
                    attack("2026-10-22T10:00", hours = null),
                marks = periodDays(OCTOBER, 5..14)
            ),
            previous = entries(
                // The attack on 26 September is after 24 September, so it is not compared.
                attacks =
                (1..5).map { attack("2026-09-%02dT10:00".format(it)) } +
                    attack("2026-09-26T10:00"),
                marks = periodDays(SEPTEMBER, 10..19)
            )
        )

        assertEquals(4, actual.migraineDays)
        assertEquals(
            ProgressDomainModel(-1, ProgressDomainModel.ComparedWith.SameDaysLastMonth),
            actual.progress
        )
    }

    @Test
    fun `Given too few days with entries or no month before then no progress`() {
        val current = entries(marks = periodDays(SEPTEMBER, 1..12))

        assertNull(
            september(
                current,
                previous = entries(
                    marks = periodDays(
                        YearMonth.of(2026, 8),
                        1..8
                    )
                )
            ).progress
        )
        assertNull(september(current, previous = null).progress)
    }
}
