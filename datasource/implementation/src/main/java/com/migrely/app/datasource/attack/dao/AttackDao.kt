package com.migrely.app.datasource.attack.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.migrely.app.datasource.attack.model.AttackLocalModel
import com.migrely.app.datasource.attack.model.AttackWithPointsLocalModel
import com.migrely.app.datasource.attack.model.IntensityPointLocalModel

@Dao
abstract class AttackDao {
    @Transaction
    @Query("SELECT * FROM attack WHERE end_epoch_millis IS NULL LIMIT 1")
    abstract fun inProgress(): AttackWithPointsLocalModel?

    @Transaction
    @Query("SELECT * FROM attack WHERE id = :attackId")
    abstract fun byId(attackId: String): AttackWithPointsLocalModel?

    @Transaction
    @Query(
        "SELECT * FROM attack WHERE end_epoch_millis IS NOT NULL " +
            "AND start_epoch_millis < :toEpochMillis AND end_epoch_millis > :fromEpochMillis " +
            "ORDER BY start_epoch_millis"
    )
    abstract fun endedTouching(
        fromEpochMillis: Long,
        toEpochMillis: Long
    ): List<AttackWithPointsLocalModel>

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
        "UPDATE attack SET start_epoch_millis = :startEpochMillis, " +
            "updated_epoch_millis = :nowEpochMillis " +
            "WHERE id = :attackId"
    )
    abstract fun updateStart(attackId: String, startEpochMillis: Long, nowEpochMillis: Long)

    @Query(
        "SELECT MAX(end_epoch_millis) FROM attack WHERE id != :attackId " +
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
        "UPDATE attack SET long_check_confirmed_epoch_millis = :confirmedEpochMillis, " +
            "long_check_disabled = :disabled, updated_epoch_millis = :nowEpochMillis WHERE id = :attackId"
    )
    abstract fun updateLongCheck(
        attackId: String,
        confirmedEpochMillis: Long?,
        disabled: Boolean,
        nowEpochMillis: Long
    )

    /**
     * Inserts [attack] unless an attack is already in progress, in one transaction so that two quick taps can never
     * create two attacks in progress (BRD EC-ATK-01). Returns the attack in progress, new or existing, and whether it
     * was created.
     */
    @Transaction
    open fun startIfNoneInProgress(attack: AttackLocalModel): StartOutcome {
        val existing = inProgress()
        if (existing != null) return StartOutcome(existing, created = false)
        insertAttack(attack)
        return StartOutcome(AttackWithPointsLocalModel(attack, emptyList()), created = true)
    }

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

    data class StartOutcome(val attack: AttackWithPointsLocalModel, val created: Boolean)
}
