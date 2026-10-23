package com.migrely.app.attack.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attack.domain.model.AttackDetailsDomainModel
import com.migrely.app.attack.domain.model.AttackDetailsLookupResultDomainModel
import com.migrely.app.attack.domain.model.AttackRangeDomainModel
import com.migrely.app.attack.domain.model.TimeFieldDomainModel
import com.migrely.app.attack.domain.model.UpdateAttackTimesRequestDomainModel
import com.migrely.app.attack.domain.model.UpdateAttackTimesResultDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.repository.FindOverlappingAttacksRepository
import com.migrely.app.attack.domain.repository.GetAttackDetailsRepository
import com.migrely.app.attack.domain.repository.GetCurrentTimeRepository
import com.migrely.app.attack.domain.repository.UpdateAttackTimesRepository
import com.migrely.app.attack.domain.usecase.AttackTimesPlacer.Placement
import com.migrely.app.coroutine.CoroutineContextProvider

/**
 * Sets an attack's start and end (BRD EC-ATK-03, EC-ATK-04). The end of an attack in progress stays empty. Times may
 * not overlap other attacks, which are named so they can be merged with [MergeAttacksUseCase] (EC-ATK-05, EC-ATK-15);
 * an overlap under a minute snaps (EC-ATK-18). Pain entries left outside the new times are removed in the same step and
 * returned, so the change can be undone (BRD EC-ATK-09).
 */
class UpdateAttackTimesUseCase(
    private val getAttackDetailsRepository: GetAttackDetailsRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    findOverlappingAttacksRepository: FindOverlappingAttacksRepository,
    private val updateAttackTimesRepository: UpdateAttackTimesRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<
    UpdateAttackTimesRequestDomainModel,
    UpdateAttackTimesResultDomainModel
    >(coroutineContextProvider) {
    private val placer = AttackTimesPlacer(findOverlappingAttacksRepository)

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
        val requestedEnd = if (attack.isInProgress) null else request.end ?: attack.end
        when {
            request.start.isAfter(latestAllowed) ->
                return UpdateAttackTimesResultDomainModel.InFuture(TimeFieldDomainModel.Start)

            requestedEnd != null && requestedEnd.isAfter(latestAllowed) ->
                return UpdateAttackTimesResultDomainModel.InFuture(TimeFieldDomainModel.End)

            requestedEnd != null && !requestedEnd.isAfter(request.start) ->
                return UpdateAttackTimesResultDomainModel.EndBeforeStart
        }
        val times = when (
            val placement = placer.place(
                attack.id,
                AttackRangeDomainModel(request.start, requestedEnd),
                now
            )
        ) {
            is Placement.Free -> placement.times
            is Placement.Overlapping -> return UpdateAttackTimesResultDomainModel.Overlaps(
                placement.attacks.map(AttackDetailsDomainModel::range)
            )

            Placement.EndNotAfterStart -> return UpdateAttackTimesResultDomainModel.EndBeforeStart
            Placement.Failed -> return UpdateAttackTimesResultDomainModel.Failed
        }
        val start = times.start
        val end = times.end
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
