package com.migrely.app.history.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.history.domain.model.AttacksTouchingResultDomainModel
import com.migrely.app.history.domain.model.EarliestAttackResultDomainModel
import com.migrely.app.history.domain.model.HistoryAttackDomainModel
import com.migrely.app.history.domain.model.HistoryDayDomainModel
import com.migrely.app.history.domain.model.HistoryMonthDomainModel
import com.migrely.app.history.domain.model.HistoryMonthResultDomainModel
import com.migrely.app.history.domain.repository.GetAttacksTouchingRepository
import com.migrely.app.history.domain.repository.GetCurrentTimeRepository
import com.migrely.app.history.domain.repository.GetEarliestAttackRepository
import com.migrely.app.history.domain.repository.GetRecentlyDeletedAttackRepository
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth

/** Zone offsets differ by at most 26 hours, so two days on each side cover any zone an attack was logged in. */
private const val ZONE_MARGIN_DAYS = 2L

/**
 * One month of History (BRD HIS-F1, HIS-F2, D29). The request is the month asked for, or null for the current one; the
 * month shown never precedes the month of the first attack nor follows the current month (BRD EC-HIS-03).
 *
 * An attack belongs to every day it touches in the zone where it started (BRD EC-ATK-06, EC-TIM-02): from its start
 * date to the date just before its end, or to today while it is in progress.
 */
class GetHistoryMonthUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getEarliestAttackRepository: GetEarliestAttackRepository,
    private val getAttacksTouchingRepository: GetAttacksTouchingRepository,
    private val getRecentlyDeletedAttackRepository: GetRecentlyDeletedAttackRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<YearMonth?, HistoryMonthResultDomainModel>(
    coroutineContextProvider
) {
    override fun executeInBackground(request: YearMonth?): HistoryMonthResultDomainModel {
        val now = getCurrentTimeRepository.currentTime()
        val today = now.instant.atZone(now.zone).toLocalDate()
        val currentMonth = YearMonth.from(today)
        val earliestMonth = when (val earliest = getEarliestAttackRepository.earliestAttack()) {
            is EarliestAttackResultDomainModel.Found -> minOf(
                YearMonth.from(earliest.start.atZone(earliest.zone)),
                currentMonth
            )

            EarliestAttackResultDomainModel.None -> currentMonth
            EarliestAttackResultDomainModel.Failed -> return HistoryMonthResultDomainModel.Failed
        }
        val month = (request ?: currentMonth).coerceIn(earliestMonth, currentMonth)
        val from = month.atDay(1)
            .minusDays(ZONE_MARGIN_DAYS)
            .atStartOfDay(now.zone)
            .toInstant()
        val to = month.atEndOfMonth()
            .plusDays(1 + ZONE_MARGIN_DAYS)
            .atStartOfDay(now.zone)
            .toInstant()
        val attacks = when (val result = getAttacksTouchingRepository.attacksTouching(from, to)) {
            is AttacksTouchingResultDomainModel.Loaded -> result.attacks
            AttacksTouchingResultDomainModel.Failed -> return HistoryMonthResultDomainModel.Failed
        }
        val datesOfAttacks = attacks.associateWith { it.dates(now.instant) }
        val monthDates = month.atDay(1)..month.atEndOfMonth()
        return HistoryMonthResultDomainModel.Loaded(
            month = HistoryMonthDomainModel(
                month = month,
                today = today,
                earliestMonth = earliestMonth,
                days = (1..month.lengthOfMonth()).map { dayOfMonth ->
                    val date = month.atDay(dayOfMonth)
                    HistoryDayDomainModel(
                        date = date,
                        attacks = attacks
                            .filter { date in datesOfAttacks.getValue(it) }
                            .sortedBy(HistoryAttackDomainModel::start)
                    )
                },
                attacks = attacks
                    .filter { datesOfAttacks.getValue(it).overlaps(monthDates) }
                    .sortedByDescending(HistoryAttackDomainModel::start)
            ),
            recentlyDeleted = getRecentlyDeletedAttackRepository.recentlyDeletedAttack()
        )
    }

    private fun HistoryAttackDomainModel.dates(now: Instant): ClosedRange<LocalDate> {
        val first = start.atZone(startZone).toLocalDate()
        // An attack that ends exactly at midnight does not touch the next day.
        val last = (end?.minusNanos(1) ?: now).atZone(startZone).toLocalDate()
        return first..maxOf(first, last)
    }

    private fun ClosedRange<LocalDate>.overlaps(other: ClosedRange<LocalDate>) =
        start <= other.endInclusive && endInclusive >= other.start
}
