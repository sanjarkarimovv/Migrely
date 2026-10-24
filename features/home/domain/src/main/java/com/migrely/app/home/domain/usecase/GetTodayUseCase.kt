package com.migrely.app.home.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attackdays.domain.AttackDays
import com.migrely.app.coroutine.CoroutineContextProvider
import com.migrely.app.home.domain.model.AttackDomainModel
import com.migrely.app.home.domain.model.AttackSummaryDomainModel
import com.migrely.app.home.domain.model.CurrentTimeDomainModel
import com.migrely.app.home.domain.model.DayMarksDomainModel
import com.migrely.app.home.domain.model.DayMarksResultDomainModel
import com.migrely.app.home.domain.model.TodayDomainModel
import com.migrely.app.home.domain.model.TodayResultDomainModel
import com.migrely.app.home.domain.model.TodaySnapshotDomainModel
import com.migrely.app.home.domain.model.TodaySnapshotResultDomainModel
import com.migrely.app.home.domain.repository.GetCurrentTimeRepository
import com.migrely.app.home.domain.repository.GetDayMarksRepository
import com.migrely.app.home.domain.repository.GetTodayRepository
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

/** An attack open this long triggers the "Still going?" check (BRD EC-ATK-02, SAF-F3). */
val LONG_ATTACK_THRESHOLD: Duration = Duration.ofHours(72)

/**
 * Today's card (BRD 18.3 A). An ended attack counts for today when the days it touches, in the zone where it started,
 * include the device's current date (BRD EC-TIM-02, EC-ATK-06, D36): the rule the calendar and History use.
 */
class GetTodayUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val getTodayRepository: GetTodayRepository,
    private val getDayMarksRepository: GetDayMarksRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<Unit, TodayResultDomainModel>(coroutineContextProvider) {
    override fun executeInBackground(request: Unit): TodayResultDomainModel {
        val now = getCurrentTimeRepository.currentTime()
        val today = now.instant.atZone(now.zone).toLocalDate()
        val from = AttackDays.windowStart(today, now.zone)
        val to = AttackDays.windowEnd(today, now.zone)
        return when (val result = getTodayRepository.today(from, to)) {
            is TodaySnapshotResultDomainModel.Loaded -> {
                // Marks that cannot be read never hide today's attacks: the card shows no mark.
                val marks = when (val read = getDayMarksRepository.dayMarks(today)) {
                    is DayMarksResultDomainModel.Loaded -> read.marks
                    DayMarksResultDomainModel.Failed ->
                        DayMarksDomainModel(period = false, painFree = false)
                }
                TodayResultDomainModel.Loaded(
                    today = result.snapshot.toToday(now, today, marks.painFree),
                    recentlyDeleted = result.snapshot.recentlyDeleted,
                    periodToday = marks.period
                )
            }
            TodaySnapshotResultDomainModel.Failed -> TodayResultDomainModel.Failed
        }
    }

    private fun TodaySnapshotDomainModel.toToday(
        now: CurrentTimeDomainModel,
        today: LocalDate,
        markedPainFree: Boolean
    ): TodayDomainModel {
        val attack = attackInProgress
        val endedAttacksTouchingToday = endedAttacks.filter { it.touches(today) }
        return when {
            attack != null -> TodayDomainModel.AttackInProgress(
                attack = attack,
                longAttackCheckDue = attack.isLongAttackCheckDue(now.instant)
            )

            // An attack always wins over a pain-free mark (BRD EC-DAY-01).
            endedAttacksTouchingToday.isEmpty() && markedPainFree -> TodayDomainModel.PainFree

            endedAttacksTouchingToday.isEmpty() -> TodayDomainModel.Calm

            else -> TodayDomainModel.AttacksEnded(
                count = endedAttacksTouchingToday.size,
                highestRecordedIntensity = endedAttacksTouchingToday.mapNotNull(
                    AttackDomainModel::peakIntensity
                )
                    .maxOrNull(),
                anyIntensityNotRecorded = endedAttacksTouchingToday.any {
                    it.peakIntensity == null
                },
                attacks = endedAttacksTouchingToday
                    .sortedBy(AttackDomainModel::start)
                    .map { attack ->
                        AttackSummaryDomainModel(
                            id = attack.id,
                            start = attack.start,
                            startZone = attack.startZone,
                            end = requireNotNull(attack.end) { "An ended attack has an end" },
                            peakIntensity = attack.peakIntensity
                        )
                    }
            )
        }
    }

    private fun AttackDomainModel.touches(date: LocalDate): Boolean {
        val ended = end ?: return false
        return date in AttackDays.datesTouched(start, startZone, ended, ended)
    }

    private fun AttackDomainModel.isLongAttackCheckDue(now: Instant): Boolean {
        if (longAttackCheck.disabled) return false
        val since = longAttackCheck.confirmedAt ?: start
        return Duration.between(since, now) >= LONG_ATTACK_THRESHOLD
    }
}
