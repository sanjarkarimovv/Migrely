package com.migrely.app.attack.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attack.domain.model.AttackDetailsDomainModel
import com.migrely.app.attack.domain.model.AttackRangeDomainModel
import com.migrely.app.attack.domain.model.CreatePastAttackRequestDomainModel
import com.migrely.app.attack.domain.model.CreatePastAttackResultDomainModel
import com.migrely.app.attack.domain.model.OverlapLookupResultDomainModel
import com.migrely.app.attack.domain.model.StorePastAttackResultDomainModel
import com.migrely.app.attack.domain.model.TimeFieldDomainModel
import com.migrely.app.attack.domain.repository.FindOverlappingAttacksRepository
import com.migrely.app.attack.domain.repository.GetCurrentTimeRepository
import com.migrely.app.attack.domain.repository.StorePastAttackRepository
import com.migrely.app.attack.domain.usecase.AttackTimesPlacer.Placement
import com.migrely.app.coroutine.CoroutineContextProvider
import java.time.Instant

/**
 * Adds an attack that has already ended (BRD ATK-F3), in the device's zone. A start after now, an end more than
 * [CLOCK_TOLERANCE] after now and an end not after the start are refused (EC-ATK-03, EC-ATK-04, QA #89). Overlapping attacks are named, not stored
 * (EC-ATK-05, EC-ATK-15); merging them is [MergeAttacksUseCase].
 */
class CreatePastAttackUseCase(
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    findOverlappingAttacksRepository: FindOverlappingAttacksRepository,
    private val storePastAttackRepository: StorePastAttackRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<
    CreatePastAttackRequestDomainModel,
    CreatePastAttackResultDomainModel
    >(coroutineContextProvider) {
    private val placer = AttackTimesPlacer(findOverlappingAttacksRepository)

    override fun executeInBackground(
        request: CreatePastAttackRequestDomainModel
    ): CreatePastAttackResultDomainModel {
        val now = getCurrentTimeRepository.currentTime()
        val latestAllowed = now.instant.plus(CLOCK_TOLERANCE)
        when {
            request.start.isAfter(now.instant) ->
                return CreatePastAttackResultDomainModel.InFuture(TimeFieldDomainModel.Start)

            request.end.isAfter(latestAllowed) ->
                return CreatePastAttackResultDomainModel.InFuture(TimeFieldDomainModel.End)

            !request.end.isAfter(request.start) ->
                return CreatePastAttackResultDomainModel.EndBeforeStart
        }
        val requested = AttackRangeDomainModel(request.start, request.end)
        val times = when (val placement = placer.place(null, requested, now.instant)) {
            is Placement.Free -> placement.times
            is Placement.Overlapping -> return overlaps(placement.attacks)
            Placement.EndNotAfterStart -> return CreatePastAttackResultDomainModel.EndBeforeStart
            Placement.Failed -> return CreatePastAttackResultDomainModel.Failed
        }
        val end = times.end ?: request.end
        return when (
            val stored = storePastAttackRepository.storePastAttack(times.start, now.zone, end)
        ) {
            is StorePastAttackResultDomainModel.Stored ->
                CreatePastAttackResultDomainModel.Created(stored.attackId)

            // Another attack was written in between, for example by a second tap (EC-ATK-17).
            StorePastAttackResultDomainModel.Overlapping -> overlapsNow(times, now.instant)
            StorePastAttackResultDomainModel.StorageFull ->
                CreatePastAttackResultDomainModel.StorageFull

            StorePastAttackResultDomainModel.Failed -> CreatePastAttackResultDomainModel.Failed
        }
    }

    private fun overlapsNow(times: AttackRangeDomainModel, now: Instant) =
        when (val lookup = placer.lookUp(null, times, now)) {
            is OverlapLookupResultDomainModel.Found -> overlaps(lookup.attacks)
            OverlapLookupResultDomainModel.None,
            OverlapLookupResultDomainModel.Failed -> CreatePastAttackResultDomainModel.Failed
        }

    private fun overlaps(attacks: List<AttackDetailsDomainModel>) =
        CreatePastAttackResultDomainModel.Overlaps(attacks.map(AttackDetailsDomainModel::range))
}
