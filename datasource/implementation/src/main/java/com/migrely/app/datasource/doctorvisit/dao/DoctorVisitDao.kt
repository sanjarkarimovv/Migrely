package com.migrely.app.datasource.doctorvisit.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.migrely.app.datasource.doctorvisit.model.DoctorVisitLocalModel

/** Doctor visit queries (database version 7, design D2). Dates are ISO text, so they compare in order. */
@Dao
abstract class DoctorVisitDao {
    @Query("SELECT * FROM doctor_visit WHERE date >= :today ORDER BY date LIMIT 1")
    abstract fun next(today: String): DoctorVisitLocalModel?

    @Query("SELECT MAX(date) FROM doctor_visit WHERE date < :today")
    abstract fun lastDate(today: String): String?

    @Query("DELETE FROM doctor_visit WHERE date >= :today")
    abstract fun deleteUpcoming(today: String)

    /** A row with the same ID or date gives way. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(visit: DoctorVisitLocalModel)

    /** [visit] becomes the only visit on or after [today]. */
    @Transaction
    open fun replaceUpcoming(visit: DoctorVisitLocalModel, today: String) {
        deleteUpcoming(today)
        insert(visit)
    }

    /** Every visit on or after [today] goes; the one that was next is returned. */
    @Transaction
    open fun removeUpcoming(today: String): DoctorVisitLocalModel? = next(today)?.also {
        deleteUpcoming(today)
    }

    /**
     * BRD D81's question, as the report asks it: an attack touching the times, an intake taken in them, or a pain-free
     * date in the dates. An attack being deleted, and an intake hidden with one, are left out.
     */
    @Query(
        "SELECT EXISTS(SELECT 1 FROM attack WHERE deleted_epoch_millis IS NULL " +
            "AND start_epoch_millis < :toEpochMillis " +
            "AND (end_epoch_millis IS NULL OR end_epoch_millis >= :fromEpochMillis)) " +
            "OR EXISTS(SELECT 1 FROM medication_intake " +
            "WHERE taken_epoch_millis >= :fromEpochMillis " +
            "AND taken_epoch_millis < :toEpochMillis " +
            "AND (attack_id IS NULL " +
            "OR attack_id IN (SELECT id FROM attack WHERE deleted_epoch_millis IS NULL))) " +
            "OR EXISTS(SELECT 1 FROM day_mark WHERE pain_free = 1 AND date BETWEEN :fromDate AND :toDate)"
    )
    abstract fun anythingLogged(
        fromEpochMillis: Long,
        toEpochMillis: Long,
        fromDate: String,
        toDate: String
    ): Boolean
}
