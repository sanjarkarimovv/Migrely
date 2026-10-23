package com.migrely.app.attack.domain.usecase

import com.migrely.app.attack.domain.model.AttackDetailsDomainModel
import com.migrely.app.attack.domain.model.AttackRangeDomainModel
import com.migrely.app.attack.domain.model.OverlapLookupResultDomainModel
import com.migrely.app.attack.domain.repository.FindOverlappingAttacksRepository
import java.time.Duration
import java.time.Instant

private val SAME_MINUTE: Duration = Duration.ofMinutes(1)

/**
 * Places new times among the other attacks (BRD EC-ATK-05, EC-ATK-15, EC-ATK-18). Times are picked in whole minutes
 * while stored times keep their seconds, so an overlap shorter than a minute means the user meant to touch the other
 * attack: the time snaps to that attack's boundary and the overlaps are looked up again.
 */
internal class AttackTimesPlacer(
    private val findOverlappingAttacksRepository: FindOverlappingAttacksRepository
) {
    fun place(excludingId: String?, times: AttackRangeDomainModel, now: Instant): Placement {
        val overlapping = when (val lookup = lookUp(excludingId, times, now)) {
            is OverlapLookupResultDomainModel.Found -> lookup.attacks
            OverlapLookupResultDomainModel.None -> return Placement.Free(times)
            OverlapLookupResultDomainModel.Failed -> return Placement.Failed
        }
        val snapped = snap(times, overlapping.map(AttackDetailsDomainModel::range))
        if (snapped == times) return Placement.Overlapping(times, overlapping)
        val snappedEnd = snapped.end
        if (snappedEnd != null && !snappedEnd.isAfter(snapped.start)) {
            return Placement.EndNotAfterStart
        }
        return when (val lookup = lookUp(excludingId, snapped, now)) {
            is OverlapLookupResultDomainModel.Found ->
                Placement.Overlapping(snapped, lookup.attacks)

            OverlapLookupResultDomainModel.None -> Placement.Free(snapped)
            OverlapLookupResultDomainModel.Failed -> Placement.Failed
        }
    }

    /** The attacks that overlap [times]; times still in progress run to [now]. */
    fun lookUp(excludingId: String?, times: AttackRangeDomainModel, now: Instant) =
        findOverlappingAttacksRepository.overlappingAttacks(
            excludingId,
            times.start,
            times.end ?: maxOf(now, times.start)
        )

    private fun snap(
        times: AttackRangeDomainModel,
        others: List<AttackRangeDomainModel>
    ): AttackRangeDomainModel {
        var start = times.start
        var end = times.end
        others.forEach { other ->
            val otherEnd = other.end
            val currentEnd = end
            if (!other.start.isAfter(start) && otherEnd != null && otherEnd.isAfter(start) &&
                Duration.between(start, otherEnd) < SAME_MINUTE
            ) {
                start = otherEnd
            } else if (other.start.isAfter(start) && currentEnd != null &&
                currentEnd.isAfter(other.start) &&
                Duration.between(other.start, currentEnd) < SAME_MINUTE
            ) {
                end = other.start
            }
        }
        return AttackRangeDomainModel(start, end)
    }

    sealed interface Placement {
        /** Nothing overlaps [times], which may have snapped. */
        data class Free(val times: AttackRangeDomainModel) : Placement

        /** [times], which may have snapped, still overlap [attacks], oldest first. */
        data class Overlapping(
            val times: AttackRangeDomainModel,
            val attacks: List<AttackDetailsDomainModel>
        ) : Placement

        /** Snapping left the end at or before the start. */
        data object EndNotAfterStart : Placement

        data object Failed : Placement
    }
}
