package com.migrely.app.history.domain.usecase

import com.migrely.app.history.domain.model.AttackMedicationIntakeDomainModel
import com.migrely.app.history.domain.model.DayMarkDomainModel
import com.migrely.app.history.domain.model.HistoryAttackDomainModel
import com.migrely.app.history.domain.model.MonthStatsDomainModel
import com.migrely.app.history.domain.model.PainLevelDomainModel
import com.migrely.app.history.domain.model.ProgressDomainModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.floor

private const val CHRONIC_MIGRAINE_DAYS = 15
private const val MIN_DAYS_WITH_ENTRIES = 10
private const val HALF = 0.5

/** What was logged around a run of days: attacks touching it, its day marks and its attack-medication intakes. */
internal data class PeriodEntries(
    val attacks: List<HistoryAttackDomainModel>,
    val marks: List<DayMarkDomainModel>,
    val intakes: List<AttackMedicationIntakeDomainModel>
)

/**
 * A month's numbers (BRD STA-F1, STA-F2, EC-UX-14, D53–D56), counted from the month's first day to [last]: its end, or
 * today in the current month. [previous] holds the previous month's entries, or null when they could not be read.
 *
 * - An attack counts once, in the month it started in its own zone; a migraine day is a date an attack touches.
 * - A pain-free day is marked so and touched by no attack; a medication day has an intake in the zone it was taken in.
 * - A past month compares with the whole month before; the current month with the same dates of the month before.
 *   Either needs 10 days with entries in both periods, because a month without entries is not a month without pain.
 */
internal class MonthStatsCalculator {
    fun calculate(
        month: YearMonth,
        last: LocalDate,
        now: Instant,
        current: PeriodEntries,
        previous: PeriodEntries?
    ): MonthStatsDomainModel {
        val days = month.atDay(1)..last
        val migraineDates = current.migraineDates(days, now)
        val startedInMonth = current.attacks.filter { it.startDate() in days }
        return MonthStatsDomainModel(
            migraines = startedInMonth.size,
            migraineDays = migraineDates.size,
            painFreeDays = current.marks.count {
                it.painFree && it.date in days && it.date !in migraineDates
            },
            averagePeak = averagePeak(startedInMonth),
            attackMedicationDays = current.intakeDates(days).size,
            chronic = migraineDates.size >= CHRONIC_MIGRAINE_DAYS,
            progress = previous?.let { progress(month, last, now, current, it) }
        )
    }

    private fun progress(
        month: YearMonth,
        last: LocalDate,
        now: Instant,
        current: PeriodEntries,
        previous: PeriodEntries
    ): ProgressDomainModel? {
        val previousMonth = month.minusMonths(1)
        val wholeMonth = last == month.atEndOfMonth()
        val previousLast = if (wholeMonth) {
            previousMonth.atEndOfMonth()
        } else {
            previousMonth.atDay(minOf(last.dayOfMonth, previousMonth.lengthOfMonth()))
        }
        val days = month.atDay(1)..last
        val previousDays = previousMonth.atDay(1)..previousLast
        if (current.daysWithEntries(days, now) < MIN_DAYS_WITH_ENTRIES ||
            previous.daysWithEntries(previousDays, now) < MIN_DAYS_WITH_ENTRIES
        ) {
            return null
        }
        return ProgressDomainModel(
            difference = current.migraineDates(days, now).size -
                previous.migraineDates(previousDays, now).size,
            comparedWith = if (wholeMonth) {
                ProgressDomainModel.ComparedWith.WholeMonth(previousMonth)
            } else {
                ProgressDomainModel.ComparedWith.SameDaysLastMonth
            }
        )
    }

    /** The mean of the levels, rounded to the nearest with halves up (BRD D56). */
    private fun averagePeak(attacks: List<HistoryAttackDomainModel>): PainLevelDomainModel? {
        val ranks = attacks.mapNotNull { it.peakIntensity?.let(PainLevelDomainModel::of)?.rank }
        if (ranks.isEmpty()) return null
        val rank = floor(ranks.average() + HALF).toInt()
        return PainLevelDomainModel.entries.first { it.rank == rank }
    }

    private fun HistoryAttackDomainModel.startDate(): LocalDate =
        start.atZone(startZone).toLocalDate()

    private fun PeriodEntries.migraineDates(
        days: ClosedRange<LocalDate>,
        now: Instant
    ): Set<LocalDate> = attacks.flatMapTo(mutableSetOf()) { attack ->
        val touched = attack.datesTouched(now)
        generateSequence(maxOf(touched.start, days.start)) { it.plusDays(1) }
            .takeWhile { it <= minOf(touched.endInclusive, days.endInclusive) }
            .toList()
    }

    private fun PeriodEntries.intakeDates(days: ClosedRange<LocalDate>): Set<LocalDate> =
        intakes.map { it.takenAt.atZone(it.zone).toLocalDate() }.filterTo(mutableSetOf()) {
            it in
                days
        }

    private fun PeriodEntries.daysWithEntries(days: ClosedRange<LocalDate>, now: Instant): Int = (
        migraineDates(days, now) +
            marks.filter { (it.period || it.painFree) && it.date in days }.map { it.date } +
            intakeDates(days)
        ).size
}
