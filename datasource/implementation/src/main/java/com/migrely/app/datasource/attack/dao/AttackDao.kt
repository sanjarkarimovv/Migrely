package com.migrely.app.datasource.attack.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.migrely.app.datasource.attack.model.AttackLocalModel
import com.migrely.app.datasource.attack.model.AttackTagLocalModel
import com.migrely.app.datasource.attack.model.AttackWithPointsLocalModel
import com.migrely.app.datasource.attack.model.EarliestStartLocalModel
import com.migrely.app.datasource.attack.model.IntensityPointLocalModel
import kotlinx.coroutines.flow.Flow

/**
 * Attack queries. Every read except [deletedById] and [recentlyDeleted] skips deleted attacks
 * (`deleted_epoch_millis IS NOT NULL`), which wait for their undo window to pass before they are purged (BRD D23).
 */
/** The clock tolerance of BRD EC-ATK-03: an ended attack may end up to this long after now. */
private const val CLOCK_TOLERANCE_MILLIS = 5 * 60_000L

@Suppress("TooManyFunctions")
@Dao
abstract class AttackDao {
    @Transaction
    @Query(
        "SELECT * FROM attack WHERE end_epoch_millis IS NULL AND deleted_epoch_millis IS NULL LIMIT 1"
    )
    abstract fun inProgress(): AttackWithPointsLocalModel?

    /** Read again by Room whenever the attack table changes. */
    @Query(
        "SELECT EXISTS(SELECT 1 FROM attack " +
            "WHERE end_epoch_millis IS NULL AND deleted_epoch_millis IS NULL)"
    )
    abstract fun inProgressChanges(): Flow<Boolean>

    @Transaction
    @Query("SELECT * FROM attack WHERE id = :attackId AND deleted_epoch_millis IS NULL")
    abstract fun byId(attackId: String): AttackWithPointsLocalModel?

    @Query("SELECT * FROM attack WHERE id = :attackId AND deleted_epoch_millis IS NOT NULL")
    abstract fun deletedById(attackId: String): AttackLocalModel?

    @Transaction
    @Query(
        "SELECT * FROM attack WHERE end_epoch_millis IS NOT " +
            "NULL AND deleted_epoch_millis IS NULL " +
            "AND start_epoch_millis < :toEpochMillis AND end_epoch_millis > :fromEpochMillis " +
            "ORDER BY start_epoch_millis"
    )
    abstract fun endedTouching(
        fromEpochMillis: Long,
        toEpochMillis: Long
    ): List<AttackWithPointsLocalModel>

    /** The undeleted attack that started first; the start index serves the order (BRD EC-HIS-03). */
    @Query(
        "SELECT start_epoch_millis, start_zone_id FROM attack WHERE deleted_epoch_millis IS NULL " +
            "ORDER BY start_epoch_millis LIMIT 1"
    )
    abstract fun earliestStart(): EarliestStartLocalModel?

    /** An attack in progress is open-ended; attacks that only touch at one instant do not overlap. */
    @Transaction
    @Query(
        "SELECT * FROM attack WHERE id != :attackId AND deleted_epoch_millis IS NULL " +
            "AND start_epoch_millis < :endEpochMillis " +
            "AND (end_epoch_millis IS NULL OR end_epoch_millis > :startEpochMillis) " +
            "ORDER BY start_epoch_millis LIMIT 1"
    )
    abstract fun overlapping(
        attackId: String,
        startEpochMillis: Long,
        endEpochMillis: Long
    ): AttackWithPointsLocalModel?

    /** Every attack other than [excludingId] that overlaps, oldest first, with the same rule as [overlapping]. */
    @Transaction
    @Query(
        "SELECT * FROM attack WHERE (:excludingId IS NULL OR id != :excludingId) " +
            "AND deleted_epoch_millis IS NULL AND start_epoch_millis < :endEpochMillis " +
            "AND (end_epoch_millis IS NULL OR end_epoch_millis > :startEpochMillis) " +
            "ORDER BY start_epoch_millis"
    )
    abstract fun overlappingAll(
        excludingId: String?,
        startEpochMillis: Long,
        endEpochMillis: Long
    ): List<AttackWithPointsLocalModel>

    /** Whether any attack overlaps, with the same rule as [overlapping], without loading the attacks. */
    @Query(
        "SELECT EXISTS(SELECT 1 FROM attack WHERE deleted_epoch_millis IS NULL " +
            "AND start_epoch_millis < :endEpochMillis " +
            "AND (end_epoch_millis IS NULL OR end_epoch_millis > :startEpochMillis))"
    )
    abstract fun anyOverlapping(startEpochMillis: Long, endEpochMillis: Long): Boolean

    @Insert
    abstract fun insertAttack(attack: AttackLocalModel)

    @Insert
    abstract fun insertIntensityPoint(point: IntensityPointLocalModel)

    @Update
    abstract fun updateIntensityPoint(point: IntensityPointLocalModel)

    @Query(
        "SELECT * FROM intensity_point WHERE attack_id = :attackId " +
            "ORDER BY at_epoch_millis DESC, id DESC LIMIT 1"
    )
    abstract fun latestIntensityPoint(attackId: String): IntensityPointLocalModel?

    @Query(
        "DELETE FROM intensity_point WHERE id IN (SELECT id FROM intensity_point " +
            "WHERE attack_id = :attackId AND at_epoch_millis = :atEpochMillis AND value = :value LIMIT 1)"
    )
    abstract fun deleteIntensityPoint(attackId: String, atEpochMillis: Long, value: Int)

    @Query(
        "SELECT EXISTS(SELECT 1 FROM intensity_point INNER JOIN " +
            "attack ON attack.id = intensity_point.attack_id " +
            "WHERE intensity_point.value = :value AND attack.deleted_epoch_millis IS NULL)"
    )
    abstract fun anyIntensityPointOf(value: Int): Boolean

    @Query(
        "UPDATE attack SET start_epoch_millis = :startEpochMillis, " +
            "updated_epoch_millis = :nowEpochMillis " +
            "WHERE id = :attackId"
    )
    abstract fun updateStart(attackId: String, startEpochMillis: Long, nowEpochMillis: Long)

    @Query(
        "UPDATE attack SET start_epoch_millis = " +
            ":startEpochMillis, end_epoch_millis = :endEpochMillis, " +
            "updated_epoch_millis = :nowEpochMillis WHERE id = :attackId"
    )
    abstract fun updateTimes(
        attackId: String,
        startEpochMillis: Long,
        endEpochMillis: Long?,
        nowEpochMillis: Long
    )

    @Query(
        "SELECT MAX(end_epoch_millis) FROM attack WHERE id != " +
            ":attackId AND deleted_epoch_millis IS NULL " +
            "AND end_epoch_millis IS NOT NULL AND end_epoch_millis <= :beforeEpochMillis"
    )
    abstract fun previousEndBefore(attackId: String, beforeEpochMillis: Long): Long?

    @Query(
        "UPDATE attack SET end_epoch_millis = :endEpochMillis, " +
            "updated_epoch_millis = :nowEpochMillis " +
            "WHERE id = :attackId AND end_epoch_millis IS NULL"
    )
    abstract fun end(attackId: String, endEpochMillis: Long, nowEpochMillis: Long)

    @Query(
        "UPDATE attack SET end_epoch_millis = NULL, updated_epoch_millis = :nowEpochMillis " +
            "WHERE id = :attackId"
    )
    abstract fun clearEnd(attackId: String, nowEpochMillis: Long)

    @Query("DELETE FROM attack WHERE id = :attackId")
    abstract fun delete(attackId: String)

    @Query(
        "UPDATE attack SET deleted_epoch_millis = :nowEpochMillis, " +
            "updated_epoch_millis = :nowEpochMillis " +
            "WHERE id = :attackId AND deleted_epoch_millis IS NULL"
    )
    abstract fun softDelete(attackId: String, nowEpochMillis: Long)

    @Query(
        "UPDATE attack SET deleted_epoch_millis = NULL, updated_epoch_millis = :nowEpochMillis " +
            "WHERE id = :attackId"
    )
    abstract fun clearDeleted(attackId: String, nowEpochMillis: Long)

    @Query(
        "SELECT * FROM attack WHERE deleted_epoch_millis IS NOT NULL " +
            "AND deleted_epoch_millis >= :sinceEpochMillis " +
            "ORDER BY deleted_epoch_millis DESC LIMIT 1"
    )
    abstract fun recentlyDeleted(sinceEpochMillis: Long): AttackLocalModel?

    /** Removes deleted attacks for good; their points, tags and intakes go with them (foreign keys cascade). */
    @Query(
        "DELETE FROM attack WHERE deleted_epoch_millis IS NOT NULL AND deleted_epoch_millis < :cutoffEpochMillis"
    )
    abstract fun purgeDeletedBefore(cutoffEpochMillis: Long)

    @Query(
        "UPDATE attack SET long_check_confirmed_epoch_millis = :confirmedEpochMillis, " +
            "long_check_disabled = :disabled, updated_epoch_millis = :nowEpochMillis WHERE id = :attackId"
    )
    abstract fun updateLongCheck(
        attackId: String,
        confirmedEpochMillis: Long?,
        disabled: Boolean,
        nowEpochMillis: Long
    )

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertTag(tag: AttackTagLocalModel)

    @Query(
        "DELETE FROM attack_tag WHERE attack_id = :attackId AND kind = :kind AND tag_key = :tagKey"
    )
    abstract fun deleteTag(attackId: String, kind: String, tagKey: String)

    @Query("SELECT * FROM attack_tag WHERE attack_id IN (:attackIds)")
    abstract fun tagsOf(attackIds: List<String>): List<AttackTagLocalModel>

    @Query(
        "UPDATE intensity_point SET attack_id = :toAttackId WHERE attack_id IN (:fromAttackIds)"
    )
    abstract fun moveIntensityPoints(fromAttackIds: List<String>, toAttackId: String)

    /** The strictest long-attack check among [attackIds]: disabled if any was, and the latest confirmation. */
    @Query(
        "UPDATE attack SET long_check_disabled = " +
            "(SELECT MAX(long_check_disabled) FROM attack WHERE id IN (:attackIds)), " +
            "long_check_confirmed_epoch_millis = (SELECT MAX(long_check_confirmed_epoch_millis) " +
            "FROM attack WHERE id IN (:attackIds)) WHERE id = :survivorId"
    )
    abstract fun mergeLongCheck(survivorId: String, attackIds: List<String>)

    @Suppress("LongParameterList")
    @Query(
        "UPDATE attack SET start_epoch_millis = :startEpochMillis, start_zone_id = :startZoneId, " +
            "end_epoch_millis = :endEpochMillis, functional_impact = :impact, note = :note, " +
            "updated_epoch_millis = :nowEpochMillis WHERE id = :attackId"
    )
    abstract fun updateMerged(
        attackId: String,
        startEpochMillis: Long,
        startZoneId: String,
        endEpochMillis: Long?,
        impact: Int?,
        note: String,
        nowEpochMillis: Long
    )

    @Query("DELETE FROM attack WHERE id IN (:attackIds)")
    abstract fun deleteAll(attackIds: List<String>)

    @Query(
        "UPDATE attack SET functional_impact = :impact, updated_epoch_millis = :nowEpochMillis " +
            "WHERE id = :attackId"
    )
    abstract fun setFunctionalImpact(attackId: String, impact: Int?, nowEpochMillis: Long)

    @Query(
        "UPDATE attack SET note = :note, updated_epoch_millis = :nowEpochMillis WHERE id = :attackId"
    )
    abstract fun setNote(attackId: String, note: String, nowEpochMillis: Long)

    /**
     * Inserts [attack] unless an attack is already in progress, in one transaction so that two quick taps can never
     * create two attacks in progress (BRD EC-ATK-01). Returns the attack in progress, new or existing, and whether it
     * was created.
     *
     * An ended attack may end up to the clock tolerance after now, so the new start can fall inside it (QA #78). That
     * attack then ends at the new start, with its later pain entries moved there too, so the two only touch (BRD
     * EC-ATK-05, EC-ATK-19). An end further away, which only a phone clock moved back can leave, is not touched: a
     * silent cut of a real attack would be worse than an overlap the user can see and fix (EC-TIM-04).
     */
    @Transaction
    open fun startIfNoneInProgress(attack: AttackLocalModel): StartOutcome {
        val existing = inProgress()
        if (existing != null) return StartOutcome(existing, created = false)
        val latestEnd = attack.startEpochMillis + CLOCK_TOLERANCE_MILLIS
        movePointsOfAttacksRunningPast(attack.startEpochMillis, latestEnd)
        endAttacksRunningPast(attack.startEpochMillis, latestEnd, attack.createdEpochMillis)
        insertAttack(attack)
        return StartOutcome(AttackWithPointsLocalModel(attack, emptyList()), created = true)
    }

    @Query(
        "UPDATE intensity_point SET at_epoch_millis = :startEpochMillis " +
            "WHERE at_epoch_millis > :startEpochMillis AND attack_id IN (SELECT id FROM attack " +
            "WHERE deleted_epoch_millis IS NULL AND start_epoch_millis < :startEpochMillis " +
            "AND end_epoch_millis > :startEpochMillis AND end_epoch_millis <= :latestEndEpochMillis)"
    )
    abstract fun movePointsOfAttacksRunningPast(startEpochMillis: Long, latestEndEpochMillis: Long)

    @Query(
        "UPDATE attack SET end_epoch_millis = :startEpochMillis, " +
            "updated_epoch_millis = :nowEpochMillis " +
            "WHERE deleted_epoch_millis IS NULL AND start_epoch_millis < :startEpochMillis " +
            "AND end_epoch_millis > :startEpochMillis AND end_epoch_millis <= :latestEndEpochMillis"
    )
    abstract fun endAttacksRunningPast(
        startEpochMillis: Long,
        latestEndEpochMillis: Long,
        nowEpochMillis: Long
    )

    /** Replaces the newest point of [attackId], or adds [point] when the attack has none yet. */
    @Transaction
    open fun replaceLatestIntensityPoint(point: IntensityPointLocalModel) {
        val latest = latestIntensityPoint(point.attackId)
        if (latest == null) {
            insertIntensityPoint(point)
        } else {
            updateIntensityPoint(point.copy(id = latest.id))
        }
    }

    /** Reopens [attackId] only when no other attack is in progress. Returns false when another one is. */
    @Transaction
    open fun reopenIfNoneInProgress(attackId: String, nowEpochMillis: Long): Boolean {
        val inProgress = inProgress()
        if (inProgress != null && inProgress.attack.id != attackId) return false
        clearEnd(attackId, nowEpochMillis)
        return true
    }

    /** Sets new times and removes the points they leave outside, in one step (BRD EC-ATK-09). */
    @Transaction
    open fun updateTimesRemovingPoints(
        attackId: String,
        startEpochMillis: Long,
        endEpochMillis: Long?,
        pointsToRemove: List<IntensityPointLocalModel>,
        nowEpochMillis: Long
    ) {
        updateTimes(attackId, startEpochMillis, endEpochMillis, nowEpochMillis)
        pointsToRemove.forEach { point ->
            deleteIntensityPoint(attackId, point.atEpochMillis, point.value)
        }
    }

    /** Puts back earlier times and the points a time change removed, in one step. */
    @Transaction
    open fun restoreTimesWithPoints(
        attackId: String,
        startEpochMillis: Long,
        endEpochMillis: Long?,
        points: List<IntensityPointLocalModel>,
        nowEpochMillis: Long
    ) {
        updateTimes(attackId, startEpochMillis, endEpochMillis, nowEpochMillis)
        points.forEach(::insertIntensityPoint)
    }

    /**
     * Undoes a deletion unless the attack would become a second attack in progress or overlap another attack. The
     * range of an attack in progress runs to [nowEpochMillis].
     */
    @Transaction
    open fun restoreIfPossible(attackId: String, nowEpochMillis: Long): Boolean {
        val deleted = deletedById(attackId) ?: return false
        if (deleted.endEpochMillis == null && inProgress() != null) return false
        val end = deleted.endEpochMillis ?: maxOf(nowEpochMillis, deleted.startEpochMillis)
        if (overlapping(attackId, deleted.startEpochMillis, end) != null) return false
        clearDeleted(attackId, nowEpochMillis)
        return true
    }

    /** Inserts the ended [attack] unless another attack overlaps it, in one step (BRD EC-ATK-17). */
    @Transaction
    open fun insertEndedIfNoOverlap(attack: AttackLocalModel): Boolean {
        val end = checkNotNull(attack.endEpochMillis) { "Only an ended attack is inserted here" }
        if (anyOverlapping(attack.startEpochMillis, end)) return false
        insertAttack(attack)
        return true
    }

    /**
     * Merges [absorbedIds] into [survivorId] in one step (BRD D27): their intensity points move, their tags are copied
     * once, the survivor takes the new fields and the strictest long-attack check, and the absorbed attacks are
     * deleted, which removes their copied tags with them.
     */
    @Suppress("LongParameterList")
    @Transaction
    open fun mergeInto(
        survivorId: String,
        absorbedIds: List<String>,
        startEpochMillis: Long,
        startZoneId: String,
        endEpochMillis: Long?,
        impact: Int?,
        note: String,
        nowEpochMillis: Long
    ) {
        if (absorbedIds.isNotEmpty()) {
            moveIntensityPoints(absorbedIds, survivorId)
            moveIntakes(absorbedIds, survivorId)
            tagsOf(absorbedIds).forEach { tag -> insertTag(tag.copy(attackId = survivorId)) }
            mergeLongCheck(survivorId, absorbedIds + survivorId)
        }
        updateMerged(
            survivorId,
            startEpochMillis,
            startZoneId,
            endEpochMillis,
            impact,
            note,
            nowEpochMillis
        )
        if (absorbedIds.isNotEmpty()) deleteAll(absorbedIds)
    }

    /** Intakes of merged attacks move to the remaining one (BRD EC-MED-10, D27). */
    @Query("UPDATE medication_intake SET attack_id = :survivorId WHERE attack_id IN (:absorbedIds)")
    abstract fun moveIntakes(absorbedIds: List<String>, survivorId: String)

    @Query("UPDATE medication_intake SET attack_id = NULL WHERE attack_id = :attackId")
    abstract fun detachIntakes(attackId: String)

    /**
     * Removes an attack started by mistake for good. Its points and tags go with it; a medication logged meanwhile was
     * really taken, so its intake stays, on its own.
     */
    @Transaction
    open fun discard(attackId: String) {
        detachIntakes(attackId)
        delete(attackId)
    }

    data class StartOutcome(val attack: AttackWithPointsLocalModel, val created: Boolean)
}
