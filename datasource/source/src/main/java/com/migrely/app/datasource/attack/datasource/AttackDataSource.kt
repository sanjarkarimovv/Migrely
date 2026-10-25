package com.migrely.app.datasource.attack.datasource

import com.migrely.app.datasource.attack.model.AttackDataModel
import com.migrely.app.datasource.attack.model.AttackStartDataModel
import com.migrely.app.datasource.attack.model.AttackTagDataModel
import com.migrely.app.datasource.attack.model.CreateEndedDataModel
import com.migrely.app.datasource.attack.model.DeletedAttackDataModel
import com.migrely.app.datasource.attack.model.EarliestAttackDataModel
import com.migrely.app.datasource.attack.model.IntensityPointDataModel
import com.migrely.app.datasource.attack.model.ReopenDataModel
import com.migrely.app.datasource.attack.model.RestoreDataModel
import java.time.Instant
import kotlinx.coroutines.flow.Flow

/**
 * Attacks stored on the device. Every function but [inProgressChanges] blocks and must be called off the main thread.
 * Writes throw [com.migrely.app.datasource.exception.StorageFullDataException] when the device is full and
 * [com.migrely.app.datasource.exception.DatabaseDataException] for other storage failures.
 *
 * Deleted attacks ([softDelete]) are invisible to every read except [recentlyDeleted], can be restored for a short
 * while, and are purged for good after it (BRD D23).
 */
@Suppress("TooManyFunctions")
interface AttackDataSource {
    fun inProgress(): AttackDataModel?

    /**
     * Whether an attack is in progress, then again each time that changes, whoever changed it. A storage failure ends
     * the flow with the exceptions above.
     */
    fun inProgressChanges(): Flow<Boolean>

    fun byId(attackId: String): AttackDataModel?

    /** Ended attacks that overlap `[from, to)`. */
    fun endedTouching(from: Instant, to: Instant): List<AttackDataModel>

    /** When and where the earliest attack started, or null when none is stored. */
    fun earliestStart(): EarliestAttackDataModel?

    /**
     * Every attack other than [excludingId] that overlaps `(start, end)`, oldest first. An attack in progress counts as
     * open-ended, and attacks that only touch at one instant do not overlap.
     */
    fun overlapping(excludingId: String?, start: Instant, end: Instant): List<AttackDataModel>

    /** Atomic: stores an ended attack only when no attack overlaps it (BRD EC-ATK-17). */
    fun createEnded(start: Instant, zoneId: String, end: Instant): CreateEndedDataModel

    /**
     * Atomic (BRD D27): [survivorId] takes the given fields and the intensity points, tags and intakes of [absorbedIds],
     * keeps the strictest long-attack check of them all, and [absorbedIds] are removed for good. [functionalImpact] is 0 to 3
     * or null; [note] holds at most 1,000 characters.
     */
    @Suppress("LongParameterList")
    fun merge(
        survivorId: String,
        absorbedIds: List<String>,
        start: Instant,
        startZoneId: String,
        end: Instant?,
        functionalImpact: Int?,
        note: String
    )

    /** Atomic: starts a new attack only when none is in progress. */
    fun startIfNoneInProgress(start: Instant, zoneId: String): AttackStartDataModel

    fun addIntensityPoint(attackId: String, at: Instant, value: Int)

    fun replaceLatestIntensityPoint(attackId: String, at: Instant, value: Int)

    /** Removes one intensity point equal to [point]. */
    fun removeIntensityPoint(attackId: String, point: IntensityPointDataModel)

    /** Whether any attack that is not deleted has an intensity point of 10. */
    fun maximumRecorded(): Boolean

    fun updateStart(attackId: String, start: Instant)

    /** Atomic: sets both times and removes [pointsToRemove]. */
    fun updateTimes(
        attackId: String,
        start: Instant,
        end: Instant?,
        pointsToRemove: List<IntensityPointDataModel>
    )

    /** Atomic: sets both times and adds [points] back. */
    fun restoreTimes(
        attackId: String,
        start: Instant,
        end: Instant?,
        points: List<IntensityPointDataModel>
    )

    /** The latest end among other attacks that ended at or before [before], if any. */
    fun previousEndBefore(attackId: String, before: Instant): Instant?

    fun end(attackId: String, end: Instant)

    /** Atomic: reopens only when no other attack is in progress. */
    fun reopen(attackId: String): ReopenDataModel

    /**
     * Removes the attack for good; used to undo a start, when it should never have existed. Intakes logged meanwhile
     * stay, on their own.
     */
    fun delete(attackId: String)

    fun softDelete(attackId: String)

    /** Atomic: undoes [softDelete] unless the attack would overlap another one or be a second one in progress. */
    fun restore(attackId: String): RestoreDataModel

    /** The newest attack deleted at or after [since], if it is still restorable. */
    fun recentlyDeleted(since: Instant): DeletedAttackDataModel?

    fun updateLongAttackCheck(attackId: String, confirmedAt: Instant?, disabled: Boolean)

    /** Idempotent: selecting a selected tag or clearing a cleared one changes nothing. */
    fun setTag(attackId: String, tag: AttackTagDataModel, selected: Boolean)

    /** [impact] is 0 to 3, or null for none (BRD ATK-F5). */
    fun setFunctionalImpact(attackId: String, impact: Int?)

    /** At most 1,000 characters (BRD EC-ATK-13). */
    fun setNote(attackId: String, note: String)
}
