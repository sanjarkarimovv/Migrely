package com.migrely.app.history.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attackdays.domain.AttackDays
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.history.domain.model.AttackMedicationIntakesResultDomainModel
import com.migrely.app.history.domain.model.AttacksTouchingResultDomainModel
import com.migrely.app.history.domain.model.DayMarkDomainModel
import com.migrely.app.history.domain.model.DayMarksResultDomainModel
import com.migrely.app.history.domain.model.EarliestAttackMedicationIntakeResultDomainModel
import com.migrely.app.history.domain.model.EarliestAttackResultDomainModel
import com.migrely.app.history.domain.model.EarliestDayMarkResultDomainModel
import com.migrely.app.history.domain.model.HistoryAttackDomainModel
import com.migrely.app.history.domain.model.HistoryDayDomainModel
import com.migrely.app.history.domain.model.HistoryMonthDomainModel
import com.migrely.app.history.domain.model.HistoryMonthResultDomainModel
import com.migrely.app.history.domain.model.PeriodEntriesDomainModel
import com.migrely.app.history.domain.repository.GetAttackMedicationIntakesRepository
import com.migrely.app.history.domain.repository.GetAttacksTouchingRepository
import com.migrely.app.history.domain.repository.GetCurrentTimeRepository
import com.migrely.app.history.domain.repository.GetDayMarksRepository
import com.migrely.app.history.domain.repository.GetEarliestAttackMedicationIntakeRepository
import com.migrely.app.history.domain.repository.GetEarliestAttackRepository
import com.migrely.app.history.domain.repository.GetEarliestDayMarkRepository
import com.migrely.app.history.domain.repository.GetPeriodTrackingRepository
import com.migrely.app.history.domain.repository.GetRecentlyDeletedAttackRepository
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/**
 * One month of History (BRD HIS-F1, HIS-F2, D29). The request is the month asked for, or null for the current one; the
 * month shown never precedes the month of the first attack, the first day mark or the first
 * attack-medication intake, nor follows the current month (BRD EC-HIS-03, D51, D99).
 *
 * An attack belongs to every day it touches in the zone where it started (BRD EC-ATK-06, EC-TIM-02): from its start
 * date to the date just before its end, or to today while it is in progress. Each day carries its period mark, and is
 * pain-free only when marked so and no attack touches it (BRD EC-DAY-01, D47). The month's numbers come from
 * [MonthStatsCalculator], with the month before for the progress message (BRD D53–D56).
 *
 * The month and its numbers show once anything is logged, even with no migraine: an attack, a
 * day mark or an attack-medication intake (BRD D99).
 */
class GetHistoryMonthUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getEarliestAttackRepository: GetEarliestAttackRepository,
    private val getAttacksTouchingRepository: GetAttacksTouchingRepository,
    private val getRecentlyDeletedAttackRepository: GetRecentlyDeletedAttackRepository,
    private val getDayMarksRepository: GetDayMarksRepository,
    private val getEarliestDayMarkRepository: GetEarliestDayMarkRepository,
    private val getAttackMedicationIntakesRepository: GetAttackMedicationIntakesRepository,
    private val getEarliestIntakeRepository: GetEarliestAttackMedicationIntakeRepository,
    private val getPeriodTrackingRepository: GetPeriodTrackingRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<YearMonth?, HistoryMonthResultDomainModel>(
    coroutineContextProvider
) {
    private val monthStatsCalculator = MonthStatsCalculator()

    override fun executeInBackground(request: YearMonth?): HistoryMonthResultDomainModel {
        val now = getCurrentTimeRepository.currentTime()
        val today = now.instant.atZone(now.zone).toLocalDate()
        val currentMonth = YearMonth.from(today)
        val earliest = getEarliestAttackRepository.earliestAttack()
        val earliestAttackMonth = when (earliest) {
            is EarliestAttackResultDomainModel.Found ->
                YearMonth.from(earliest.start.atZone(earliest.zone))
            EarliestAttackResultDomainModel.None -> null
            EarliestAttackResultDomainModel.Failed -> return HistoryMonthResultDomainModel.Failed
        }
        // A day marked before the first attack keeps its month reachable (BRD D51).
        val earliestMarkMonth = when (val mark = getEarliestDayMarkRepository.earliestDayMark()) {
            is EarliestDayMarkResultDomainModel.Found -> YearMonth.from(mark.date)
            EarliestDayMarkResultDomainModel.None -> null
            EarliestDayMarkResultDomainModel.Failed -> return HistoryMonthResultDomainModel.Failed
        }
        // So does an attack-medication intake before either, in the zone it was taken in;
        // preventive ones never count (BRD D99).
        val earliestIntakeMonth = when (
            val intake = getEarliestIntakeRepository.earliestAttackMedicationIntake()
        ) {
            is EarliestAttackMedicationIntakeResultDomainModel.Found ->
                YearMonth.from(intake.takenAt.atZone(intake.zone))
            EarliestAttackMedicationIntakeResultDomainModel.None -> null
            EarliestAttackMedicationIntakeResultDomainModel.Failed ->
                return HistoryMonthResultDomainModel.Failed
        }
        val earliestMonth = listOfNotNull(
            earliestAttackMonth,
            earliestMarkMonth,
            earliestIntakeMonth,
            currentMonth
        ).min()
        val month = (request ?: currentMonth).coerceIn(earliestMonth, currentMonth)
        val from = AttackDays.windowStart(month.atDay(1), now.zone)
        val to = AttackDays.windowEnd(month.atEndOfMonth(), now.zone)
        val attacks = when (val result = getAttacksTouchingRepository.attacksTouching(from, to)) {
            is AttacksTouchingResultDomainModel.Loaded -> result.attacks
            AttacksTouchingResultDomainModel.Failed -> return HistoryMonthResultDomainModel.Failed
        }
        val monthMarks = when (
            val result = getDayMarksRepository.dayMarks(month.atDay(1), month.atEndOfMonth())
        ) {
            is DayMarksResultDomainModel.Loaded -> result.marks
            DayMarksResultDomainModel.Failed -> return HistoryMonthResultDomainModel.Failed
        }
        val marks = monthMarks.associateBy(DayMarkDomainModel::date)
        val intakes = when (
            val result = getAttackMedicationIntakesRepository.attackMedicationIntakes(
                from,
                to
            )
        ) {
            is AttackMedicationIntakesResultDomainModel.Loaded -> result.intakes
            AttackMedicationIntakesResultDomainModel.Failed ->
                return HistoryMonthResultDomainModel.Failed
        }
        val datesOfAttacks = attacks.associateWith { it.datesTouched(now.instant) }
        val monthDates = month.atDay(1)..month.atEndOfMonth()
        return HistoryMonthResultDomainModel.Loaded(
            month = HistoryMonthDomainModel(
                month = month,
                today = today,
                earliestMonth = earliestMonth,
                anythingLogged = earliestAttackMonth != null ||
                    earliestMarkMonth != null ||
                    earliestIntakeMonth != null,
                days = (1..month.lengthOfMonth()).map { dayOfMonth ->
                    val date = month.atDay(dayOfMonth)
                    val dayAttacks = attacks
                        .filter { date in datesOfAttacks.getValue(it) }
                        .sortedBy(HistoryAttackDomainModel::start)
                    val mark = marks[date]
                    HistoryDayDomainModel(
                        date = date,
                        attacks = dayAttacks,
                        period = mark?.period == true,
                        painFree = mark?.painFree == true && dayAttacks.isEmpty()
                    )
                },
                attacks = attacks
                    .filter { datesOfAttacks.getValue(it).overlaps(monthDates) }
                    .sortedByDescending(HistoryAttackDomainModel::start),
                stats = monthStatsCalculator.calculate(
                    month = month,
                    today = today,
                    now = now.instant,
                    current = PeriodEntriesDomainModel(attacks, monthMarks, intakes),
                    // Migraine days compared before any attack mean nothing, so no progress until one is
                    // logged, even with marks or medication (BRD D55, spec: month-stats).
                    previous = if (earliest is EarliestAttackResultDomainModel.Found) {
                        previousMonthEntries(month.minusMonths(1), now.zone)
                    } else {
                        null
                    }
                ),
                periodTracking = getPeriodTrackingRepository.periodTracking()
            ),
            recentlyDeleted = getRecentlyDeletedAttackRepository.recentlyDeletedAttack()
        )
    }

    /** The month before, for the progress message only: when any of it cannot be read, there is no message (D55). */
    private fun previousMonthEntries(month: YearMonth, zone: ZoneId): PeriodEntriesDomainModel? {
        val from = AttackDays.windowStart(month.atDay(1), zone)
        val to = AttackDays.windowEnd(month.atEndOfMonth(), zone)
        val attacks = getAttacksTouchingRepository.attacksTouching(from, to)
        val marks = getDayMarksRepository.dayMarks(month.atDay(1), month.atEndOfMonth())
        val intakes = getAttackMedicationIntakesRepository.attackMedicationIntakes(from, to)
        return if (attacks is AttacksTouchingResultDomainModel.Loaded &&
            marks is DayMarksResultDomainModel.Loaded &&
            intakes is AttackMedicationIntakesResultDomainModel.Loaded
        ) {
            PeriodEntriesDomainModel(attacks.attacks, marks.marks, intakes.intakes)
        } else {
            null
        }
    }

    private fun ClosedRange<LocalDate>.overlaps(other: ClosedRange<LocalDate>) =
        start <= other.endInclusive && endInclusive >= other.start
}
