package com.migrely.app.history.domain.usecase

import com.migrely.app.history.domain.model.HistoryAttackDomainModel
import com.migrely.app.history.domain.model.MonthStatsDomainModel
import com.migrely.app.history.domain.model.PainLevelDomainModel
import com.migrely.app.history.domain.model.PeriodEntriesDomainModel
import com.migrely.app.history.domain.model.ProgressDomainModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.floor

private const val CHRONIC_MIGRAINE_DAYS = 15
private const val MIN_DAYS_WITH_ENTRIES = 10
private const val HALF = 0.5

/**
 * A month's numbers (BRD STA-F1, STA-F2, EC-UX-14, D53–D56), counted from the month's first day to its last, or to
 * [today] in the current month; future dates never count. [previous] holds the month before, or null when there is
 * to be no progress message.
 *
 * - An attack counts once, in the month it started in its own zone; a migraine day is a date an attack touches.
 * - A pain-free day is marked so and touched by no attack; a medication day has an intake in the zone it was taken in.
 * - A past month compares with the whole month before; the current month, even on its last day, with the same dates
 *   of the month before. Either needs 10 days with entries in both periods, because a month without entries is not a
 *   month without pain.
 */
internal class MonthStatsCalculator {
    fun calculate(
        month: YearMonth,
        today: LocalDate,
        now: Instant,
        current: PeriodEntriesDomainModel,
        previous: PeriodEntriesDomainModel?
    ): MonthStatsDomainModel {
        val past = month < YearMonth.from(today)
        val days = month.atDay(1)..if (past) month.atEndOfMonth() else today
        val migraineDates = current.migraineDates(days, now)
        val startedInMonth = current.attacks.filter { it.datesTouched(now).start in days }
        return MonthStatsDomainModel(
            migraines = startedInMonth.size,
            migraineDays = migraineDates.size,
            painFreeDays = current.marks.count {
                it.painFree && it.date in days && it.date !in migraineDates
            },
            averagePeak = averagePeak(startedInMonth),
            attackMedicationDays = current.intakeDates(days).size,
            chronic = migraineDates.size >= CHRONIC_MIGRAINE_DAYS,
            progress = previous?.let {
                progress(month, past, days, migraineDates, now, current, it)
            }
        )
    }

    @Suppress("LongParameterList")
    private fun progress(
        month: YearMonth,
        past: Boolean,
        days: ClosedRange<LocalDate>,
        migraineDates: Set<LocalDate>,
        now: Instant,
        current: PeriodEntriesDomainModel,
        previous: PeriodEntriesDomainModel
    ): ProgressDomainModel? {
        val previousMonth = month.minusMonths(1)
        val previousLast = if (past) {
            previousMonth.atEndOfMonth()
        } else {
            // 30 March compares with the whole of February.
            previousMonth.atDay(minOf(days.endInclusive.dayOfMonth, previousMonth.lengthOfMonth()))
        }
        val previousDays = previousMonth.atDay(1)..previousLast
        val previousMigraineDates = previous.migraineDates(previousDays, now)
        val enoughEntries =
            current.daysWithEntries(days, migraineDates) >= MIN_DAYS_WITH_ENTRIES &&
                previous.daysWithEntries(previousDays, previousMigraineDates) >=
                MIN_DAYS_WITH_ENTRIES
        if (!enoughEntries) return null
        return ProgressDomainModel(
            difference = migraineDates.size - previousMigraineDates.size,
            comparedWith = if (past) {
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

    private fun PeriodEntriesDomainModel.migraineDates(
        days: ClosedRange<LocalDate>,
        now: Instant
    ): Set<LocalDate> = attacks.flatMapTo(mutableSetOf()) { attack ->
        val touched = attack.datesTouched(now)
        generateSequence(maxOf(touched.start, days.start)) { it.plusDays(1) }
            .takeWhile { it <= minOf(touched.endInclusive, days.endInclusive) }
            .toList()
    }

    private fun PeriodEntriesDomainModel.intakeDates(days: ClosedRange<LocalDate>): Set<LocalDate> =
        intakes
            .map { it.takenAt.atZone(it.zone).toLocalDate() }
            .filterTo(mutableSetOf()) { it in days }

    private fun PeriodEntriesDomainModel.daysWithEntries(
        days: ClosedRange<LocalDate>,
        migraineDates: Set<LocalDate>
    ): Int {
        val markedDates = marks.filter {
            (it.period || it.painFree) && it.date in days
        }.map { it.date }
        return (migraineDates + markedDates + intakeDates(days)).size
    }
}
