package com.migrely.app.attack.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attack.domain.model.AttackDetailsLookupResultDomainModel
import com.migrely.app.attack.domain.model.OverlapLookupResultDomainModel
import com.migrely.app.attack.domain.model.TimeFieldDomainModel
import com.migrely.app.attack.domain.model.UpdateAttackTimesRequestDomainModel
import com.migrely.app.attack.domain.model.UpdateAttackTimesResultDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.repository.FindOverlappingAttackRepository
import com.migrely.app.attack.domain.repository.GetAttackDetailsRepository
import com.migrely.app.attack.domain.repository.GetCurrentTimeRepository
import com.migrely.app.attack.domain.repository.UpdateAttackTimesRepository
import com.migrely.app.coroutine.CoroutineContextProvider
import java.time.Duration
import java.time.Instant

private val SAME_MINUTE: Duration = Duration.ofMinutes(1)

/**
 * Sets an attack's start and end (BRD EC-ATK-03, EC-ATK-04). The end of an attack in progress stays empty. Times may
 * not overlap another attack; merging arrives in change 1c (BRD D22). Pain entries left outside the new times are
 * removed in the same step and returned, so the change can be undone (BRD EC-ATK-09).
 */
class UpdateAttackTimesUseCase(
    private val getAttackDetailsRepository: GetAttackDetailsRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    private val findOverlappingAttackRepository: FindOverlappingAttackRepository,
    private val updateAttackTimesRepository: UpdateAttackTimesRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<
    UpdateAttackTimesRequestDomainModel,
    UpdateAttackTimesResultDomainModel
    >(coroutineContextProvider) {
    override fun executeInBackground(
        request: UpdateAttackTimesRequestDomainModel
    ): UpdateAttackTimesResultDomainModel {
        val attack = when (
            val lookup = getAttackDetailsRepository.attackDetails(
                request.attackId
            )
        ) {
            is AttackDetailsLookupResultDomainModel.Found -> lookup.details
            AttackDetailsLookupResultDomainModel.NotFound ->
                return UpdateAttackTimesResultDomainModel.NotFound
            AttackDetailsLookupResultDomainModel.Failed ->
                return UpdateAttackTimesResultDomainModel.Failed
        }
        val now = getCurrentTimeRepository.currentTime().instant
        val latestAllowed = now.plus(CLOCK_TOLERANCE)
        var start = request.start
        var end = if (attack.isInProgress) null else request.end ?: attack.end
        when {
            start.isAfter(latestAllowed) ->
                return UpdateAttackTimesResultDomainModel.InFuture(TimeFieldDomainModel.Start)

            end != null && end.isAfter(latestAllowed) ->
                return UpdateAttackTimesResultDomainModel.InFuture(TimeFieldDomainModel.End)

            end != null && !end.isAfter(start) ->
                return UpdateAttackTimesResultDomainModel.EndBeforeStart
        }
        val overlap = overlapping(attack.id, start, end, now)
        if (overlap is OverlapLookupResultDomainModel.Found) {
            // Times are picked in whole minutes while stored times keep their seconds. An overlap shorter than a
            // minute means the user meant to touch the other attack, so the time snaps to its boundary.
            val otherEnd = overlap.end
            val currentEnd = end
            when {
                !overlap.start.isAfter(start) && otherEnd != null && otherEnd.isAfter(start) &&
                    Duration.between(start, otherEnd) < SAME_MINUTE -> start = otherEnd

                overlap.start.isAfter(start) && currentEnd != null &&
                    Duration.between(overlap.start, currentEnd) < SAME_MINUTE -> end = overlap.start

                else ->
                    return UpdateAttackTimesResultDomainModel.Overlaps(overlap.start, overlap.end)
            }
            val snappedEnd = end
            if (snappedEnd != null && !snappedEnd.isAfter(start)) {
                return UpdateAttackTimesResultDomainModel.EndBeforeStart
            }
        }
        when (
            val check = if (overlap is OverlapLookupResultDomainModel.Found) {
                overlapping(
                    attack.id,
                    start,
                    end,
                    now
                )
            } else {
                overlap
            }
        ) {
            is OverlapLookupResultDomainModel.Found ->
                return UpdateAttackTimesResultDomainModel.Overlaps(check.start, check.end)

            OverlapLookupResultDomainModel.None -> Unit
            OverlapLookupResultDomainModel.Failed ->
                return UpdateAttackTimesResultDomainModel.Failed
        }
        val removedPoints = attack.intensityPoints.filter { point ->
            point.at.isBefore(start) || (end != null && point.at.isAfter(end))
        }
        return when (
            updateAttackTimesRepository.updateAttackTimes(
                attack.id,
                start,
                end,
                removedPoints
            )
        ) {
            WriteResultDomainModel.Written -> UpdateAttackTimesResultDomainModel.Updated(
                previousStart = attack.start,
                previousEnd = attack.end,
                removedPoints = removedPoints
            )

            WriteResultDomainModel.StorageFull -> UpdateAttackTimesResultDomainModel.StorageFull
            WriteResultDomainModel.Failed -> UpdateAttackTimesResultDomainModel.Failed
        }
    }

    private fun overlapping(attackId: String, start: Instant, end: Instant?, now: Instant) =
        findOverlappingAttackRepository.overlappingAttack(attackId, start, end ?: maxOf(now, start))
}
