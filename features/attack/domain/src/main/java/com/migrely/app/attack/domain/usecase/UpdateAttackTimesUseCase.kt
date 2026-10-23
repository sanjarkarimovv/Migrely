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
        val start = request.start
        val end = if (attack.isInProgress) null else request.end ?: attack.end
        when {
            start.isAfter(latestAllowed) ->
                return UpdateAttackTimesResultDomainModel.InFuture(TimeFieldDomainModel.Start)

            end != null && end.isAfter(latestAllowed) ->
                return UpdateAttackTimesResultDomainModel.InFuture(TimeFieldDomainModel.End)

            end != null && !end.isAfter(
                start
            ) -> return UpdateAttackTimesResultDomainModel.EndBeforeStart
        }
        val rangeEnd = end ?: maxOf(now, start)
        when (
            val overlap = findOverlappingAttackRepository.overlappingAttack(
                attack.id,
                start,
                rangeEnd
            )
        ) {
            is OverlapLookupResultDomainModel.Found ->
                return UpdateAttackTimesResultDomainModel.Overlaps(overlap.start, overlap.end)

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
}
