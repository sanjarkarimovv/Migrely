package com.migrely.app.attack.domain.usecase

import com.migrely.app.architecture.domain.usecase.BackgroundExecutingUseCase
import com.migrely.app.attack.domain.model.AttackDetailsDomainModel
import com.migrely.app.attack.domain.model.AttackDetailsLookupResultDomainModel
import com.migrely.app.attack.domain.model.AttackMergeDomainModel
import com.migrely.app.attack.domain.model.AttackRangeDomainModel
import com.migrely.app.attack.domain.model.CurrentTimeDomainModel
import com.migrely.app.attack.domain.model.FunctionalImpactDomainModel
import com.migrely.app.attack.domain.model.MergeAttacksRequestDomainModel
import com.migrely.app.attack.domain.model.MergeAttacksResultDomainModel
import com.migrely.app.attack.domain.model.OverlapLookupResultDomainModel
import com.migrely.app.attack.domain.model.StorePastAttackResultDomainModel
import com.migrely.app.attack.domain.model.TimeFieldDomainModel
import com.migrely.app.attack.domain.model.WriteResultDomainModel
import com.migrely.app.attack.domain.repository.FindOverlappingAttacksRepository
import com.migrely.app.attack.domain.repository.GetAttackDetailsRepository
import com.migrely.app.attack.domain.repository.GetCurrentTimeRepository
import com.migrely.app.attack.domain.repository.MergeAttacksRepository
import com.migrely.app.attack.domain.repository.StorePastAttackRepository
import com.migrely.app.attack.domain.repository.UpdateAttackTimesRepository
import com.migrely.app.attack.domain.usecase.AttackTimesPlacer.Placement
import com.migrely.app.coroutine.CoroutineContextProvider
import java.time.Instant
import java.time.ZoneId

/** Stored attacks never overlap each other, so the merged range settles after two lookups; more means a bug. */
private const val MAXIMUM_LOOKUPS = 5
private const val NOTE_SEPARATOR = "\n\n"

/**
 * Merges times with every attack they overlap (BRD EC-ATK-05, D27). The edited attack survives, or for a new attack
 * the overlapping attack that started first. The merged range covers the times, every merged attack and every pain
 * entry, and never shrinks, so a repeated merge changes nothing. When nothing overlaps any more, the times are stored
 * as they are and the result is [MergeAttacksResultDomainModel.Saved].
 */
@Suppress("LongParameterList")
class MergeAttacksUseCase(
    private val getAttackDetailsRepository: GetAttackDetailsRepository,
    private val getCurrentTimeRepository: GetCurrentTimeRepository,
    findOverlappingAttacksRepository: FindOverlappingAttacksRepository,
    private val storePastAttackRepository: StorePastAttackRepository,
    private val updateAttackTimesRepository: UpdateAttackTimesRepository,
    private val mergeAttacksRepository: MergeAttacksRepository,
    coroutineContextProvider: CoroutineContextProvider
) : BackgroundExecutingUseCase<
    MergeAttacksRequestDomainModel,
    MergeAttacksResultDomainModel
    >(coroutineContextProvider) {
    private val placer = AttackTimesPlacer(findOverlappingAttacksRepository)

    override fun executeInBackground(
        request: MergeAttacksRequestDomainModel
    ): MergeAttacksResultDomainModel {
        val edited = request.attackId?.let { attackId ->
            when (val lookup = getAttackDetailsRepository.attackDetails(attackId)) {
                is AttackDetailsLookupResultDomainModel.Found -> lookup.details
                AttackDetailsLookupResultDomainModel.NotFound ->
                    return MergeAttacksResultDomainModel.NotFound
                AttackDetailsLookupResultDomainModel.Failed ->
                    return MergeAttacksResultDomainModel.Failed
            }
        }
        val now = getCurrentTimeRepository.currentTime()
        val end = when {
            edited == null -> request.end ?: return MergeAttacksResultDomainModel.Failed
            edited.isInProgress -> null
            else -> request.end ?: edited.end
        }
        val latestAllowed = now.instant.plus(CLOCK_TOLERANCE)
        when {
            request.start.isAfter(latestAllowed) ->
                return MergeAttacksResultDomainModel.InFuture(TimeFieldDomainModel.Start)

            end != null && end.isAfter(latestAllowed) ->
                return MergeAttacksResultDomainModel.InFuture(TimeFieldDomainModel.End)

            end != null && !end.isAfter(request.start) ->
                return MergeAttacksResultDomainModel.EndBeforeStart
        }
        val requested = AttackRangeDomainModel(request.start, end)
        val (times, firstOverlaps) = when (
            val placement = placer.place(edited?.id, requested, now.instant)
        ) {
            is Placement.Free -> placement.times to emptyList()
            is Placement.Overlapping -> placement.times to placement.attacks
            Placement.EndNotAfterStart -> return MergeAttacksResultDomainModel.EndBeforeStart
            Placement.Failed -> return MergeAttacksResultDomainModel.Failed
        }
        val (members, range) = settle(edited, times, firstOverlaps, now.instant)
            ?: return MergeAttacksResultDomainModel.Failed
        return if (members.isEmpty()) {
            save(edited, range, now)
        } else {
            merge(edited, members, times.start, range, now.zone)
        }
    }

    /**
     * Grows the merge until no other attack overlaps its range: the attacks to merge and their range, or null when a
     * lookup fails or the range does not settle.
     */
    private fun settle(
        edited: AttackDetailsDomainModel?,
        times: AttackRangeDomainModel,
        overlaps: List<AttackDetailsDomainModel>,
        now: Instant
    ): Pair<List<AttackDetailsDomainModel>, AttackRangeDomainModel>? {
        var members = overlaps
        repeat(MAXIMUM_LOOKUPS) {
            val range = cover(times, listOfNotNull(edited) + members)
            val found = when (val lookup = placer.lookUp(edited?.id, range, now)) {
                is OverlapLookupResultDomainModel.Found -> lookup.attacks
                OverlapLookupResultDomainModel.None -> emptyList()
                OverlapLookupResultDomainModel.Failed -> return null
            }
            val knownIds = members.map(AttackDetailsDomainModel::id).toSet()
            if (found.all { it.id in knownIds }) return members to range
            members = (members + found).distinctBy(AttackDetailsDomainModel::id)
        }
        return null
    }

    /** Nothing overlaps: a new attack is stored, an edited one gets the range. */
    private fun save(
        edited: AttackDetailsDomainModel?,
        range: AttackRangeDomainModel,
        now: CurrentTimeDomainModel
    ): MergeAttacksResultDomainModel {
        val end = range.end
        if (edited == null) {
            if (end == null) return MergeAttacksResultDomainModel.Failed
            return when (
                val stored = storePastAttackRepository.storePastAttack(range.start, now.zone, end)
            ) {
                is StorePastAttackResultDomainModel.Stored ->
                    MergeAttacksResultDomainModel.Saved(stored.attackId)

                StorePastAttackResultDomainModel.StorageFull ->
                    MergeAttacksResultDomainModel.StorageFull

                // An attack written in between; trying again merges with it.
                StorePastAttackResultDomainModel.Overlapping,
                StorePastAttackResultDomainModel.Failed -> MergeAttacksResultDomainModel.Failed
            }
        }
        return written(
            edited.id,
            updateAttackTimesRepository.updateAttackTimes(edited.id, range.start, end, emptyList()),
            merged = false
        )
    }

    private fun merge(
        edited: AttackDetailsDomainModel?,
        members: List<AttackDetailsDomainModel>,
        requestedStart: Instant,
        range: AttackRangeDomainModel,
        deviceZone: ZoneId
    ): MergeAttacksResultDomainModel {
        val survivor = edited ?: members.minBy(AttackDetailsDomainModel::start)
        val attacks = (listOfNotNull(edited) + members).sortedBy(AttackDetailsDomainModel::start)
        // The zone of the earliest start, so the merged attack belongs to the day it began (BRD EC-TIM-01).
        val requestedZone = edited?.startZone ?: deviceZone
        val startZone = attacks
            .map { it.start to it.startZone }
            .plus(requestedStart to requestedZone)
            .minBy { (start, _) -> start }
            .second
        val merge = AttackMergeDomainModel(
            survivorId = survivor.id,
            absorbedIds = attacks.map(AttackDetailsDomainModel::id).filter { it != survivor.id },
            start = range.start,
            startZone = startZone,
            end = range.end,
            functionalImpact = attacks
                .mapNotNull(AttackDetailsDomainModel::functionalImpact)
                .maxByOrNull(FunctionalImpactDomainModel::level),
            note = joinNotes(attacks.map(AttackDetailsDomainModel::note))
        )
        return written(survivor.id, mergeAttacksRepository.mergeAttacks(merge), merged = true)
    }

    private fun written(attackId: String, result: WriteResultDomainModel, merged: Boolean) =
        when (result) {
            WriteResultDomainModel.Written -> if (merged) {
                MergeAttacksResultDomainModel.Merged(attackId)
            } else {
                MergeAttacksResultDomainModel.Saved(attackId)
            }

            WriteResultDomainModel.StorageFull -> MergeAttacksResultDomainModel.StorageFull
            WriteResultDomainModel.Failed -> MergeAttacksResultDomainModel.Failed
        }

    /** The smallest range that holds [times], every attack and every pain entry; open-ended if any is. */
    private fun cover(
        times: AttackRangeDomainModel,
        attacks: List<AttackDetailsDomainModel>
    ): AttackRangeDomainModel {
        val pointTimes = attacks.flatMap { attack -> attack.intensityPoints.map { it.at } }
        val starts = attacks.map(AttackDetailsDomainModel::start)
        val start = (listOf(times.start) + starts + pointTimes).min()
        val timesEnd = times.end
        val inProgress = attacks.any(AttackDetailsDomainModel::isInProgress)
        val end = if (timesEnd == null || inProgress) {
            null
        } else {
            val ends = attacks.mapNotNull(AttackDetailsDomainModel::end)
            (listOf(timesEnd) + ends + pointTimes).max()
        }
        return AttackRangeDomainModel(start, end)
    }

    /** Non-empty notes, oldest first, within the note limit; a character is never cut in half (BRD EC-ATK-16). */
    private fun joinNotes(notes: List<String>): String {
        val joined = notes
            .filter(String::isNotBlank)
            .joinToString(NOTE_SEPARATOR, transform = String::trim)
        if (joined.length <= MAXIMUM_NOTE_LENGTH) return joined
        val cut = if (joined[MAXIMUM_NOTE_LENGTH - 1].isHighSurrogate()) {
            MAXIMUM_NOTE_LENGTH - 1
        } else {
            MAXIMUM_NOTE_LENGTH
        }
        return joined.substring(0, cut)
    }
}
