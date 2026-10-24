package com.migrely.app.history.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.history.domain.model.AttacksTouchingResultDomainModel
import com.migrely.app.history.domain.model.DayMarkDomainModel
import com.migrely.app.history.domain.model.DayMarksResultDomainModel
import com.migrely.app.history.domain.model.EarliestAttackResultDomainModel
import com.migrely.app.history.domain.model.HistoryAttackDomainModel
import com.migrely.app.history.domain.model.HistoryDayDomainModel
import com.migrely.app.history.domain.model.HistoryMonthDomainModel
import com.migrely.app.history.domain.model.HistoryMonthResultDomainModel
import com.migrely.app.history.domain.repository.GetAttacksTouchingRepository
import com.migrely.app.history.domain.repository.GetCurrentTimeRepository
import com.migrely.app.history.domain.repository.GetDayMarksRepository
import com.migrely.app.history.domain.repository.GetEarliestAttackRepository
import com.migrely.app.history.domain.repository.GetRecentlyDeletedAttackRepository
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

/**
 * One month of History (BRD HIS-F1, HIS-F2, D29). The request is the month asked for, or null for the current one; the
 * month shown never precedes the month of the first attack nor follows the current month (BRD EC-HIS-03).
 *
 * An attack belongs to every day it touches in the zone where it started (BRD EC-ATK-06, EC-TIM-02): from its start
 * date to the date just before its end, or to today while it is in progress. Each day carries its period mark, and is
 * pain-free only when marked so and no attack touches it (BRD EC-DAY-01, D47).
 */
class GetHistoryMonthUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getEarliestAttackRepository: GetEarliestAttackRepository,
    private val getAttacksTouchingRepository: GetAttacksTouchingRepository,
    private val getRecentlyDeletedAttackRepository: GetRecentlyDeletedAttackRepository,
    private val getDayMarksRepository: GetDayMarksRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<YearMonth?, HistoryMonthResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(request: YearMonth?): HistoryMonthResultDomainModel {
        val now = getCurrentTimeRepository.currentTime()
        val today = now.instant.atZone(now.zone).toLocalDate()
        val currentMonth = YearMonth.from(today)
        val earliest = getEarliestAttackRepository.earliestAttack()
        val earliestMonth = when (earliest) {
            is EarliestAttackResultDomainModel.Found -> minOf(
                YearMonth.from(earliest.start.atZone(earliest.zone)),
                currentMonth
            )

            EarliestAttackResultDomainModel.None -> currentMonth
            EarliestAttackResultDomainModel.Failed -> return HistoryMonthResultDomainModel.Failed
        }
        val month = (request ?: currentMonth).coerceIn(earliestMonth, currentMonth)
        val from = windowStart(month.atDay(1), now.zone)
        val to = windowEnd(month.atEndOfMonth(), now.zone)
        val attacks = when (val result = getAttacksTouchingRepository.attacksTouching(from, to)) {
            is AttacksTouchingResultDomainModel.Loaded -> result.attacks
            AttacksTouchingResultDomainModel.Failed -> return HistoryMonthResultDomainModel.Failed
        }
        val marks = when (
            val result = getDayMarksRepository.dayMarks(month.atDay(1), month.atEndOfMonth())
        ) {
            is DayMarksResultDomainModel.Loaded -> result.marks.associateBy(
                DayMarkDomainModel::date
            )
            DayMarksResultDomainModel.Failed -> return HistoryMonthResultDomainModel.Failed
        }
        val datesOfAttacks = attacks.associateWith { it.datesTouched(now.instant) }
        val monthDates = month.atDay(1)..month.atEndOfMonth()
        return HistoryMonthResultDomainModel.Loaded(
            month = HistoryMonthDomainModel(
                month = month,
                today = today,
                earliestMonth = earliestMonth,
                anyAttackLogged = earliest is EarliestAttackResultDomainModel.Found,
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
                    .sortedByDescending(HistoryAttackDomainModel::start)
            ),
            recentlyDeleted = getRecentlyDeletedAttackRepository.recentlyDeletedAttack()
        )
    }

    private fun ClosedRange<LocalDate>.overlaps(other: ClosedRange<LocalDate>) =
        start <= other.endInclusive && endInclusive >= other.start
}
