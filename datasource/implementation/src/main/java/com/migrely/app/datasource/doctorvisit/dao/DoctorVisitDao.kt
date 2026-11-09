package com.migrely.app.datasource.doctorvisit.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.migrely.app.datasource.doctorvisit.model.DoctorVisitLocalModel

/**
 * Doctor visit queries (database version 7, design D2). Dates are ISO text, so they compare in order. A visit dated
 * today or earlier is never deleted here: on its day it stays the visit of today, and from the next day on it is the
 * last visit a report starts from. Upcoming means after today.
 */
@Dao
abstract class DoctorVisitDao {
    /** The first upcoming visit, or else today's. */
    @Query(
        "SELECT * FROM doctor_visit WHERE date >= :today ORDER BY (date = :today), date LIMIT 1"
    )
    abstract fun next(today: String): DoctorVisitLocalModel?

    @Query("SELECT MAX(date) FROM doctor_visit WHERE date < :today")
    abstract fun lastDate(today: String): String?

    @Query("SELECT * FROM doctor_visit WHERE date > :today ORDER BY date LIMIT 1")
    abstract fun firstUpcoming(today: String): DoctorVisitLocalModel?

    @Query("DELETE FROM doctor_visit WHERE date > :today")
    abstract fun deleteUpcoming(today: String)

    /** A row with the same ID or date gives way. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(visit: DoctorVisitLocalModel)

    /**
     * [visit] becomes the next visit: the upcoming ones give way to one dated today or later. A visit put back that is
     * already past, after midnight during an Undo, takes nothing away.
     */
    @Transaction
    open fun replaceUpcoming(visit: DoctorVisitLocalModel, today: String) {
        if (visit.date >= today) deleteUpcoming(today)
        insert(visit)
    }

    /** Every upcoming visit goes; the first of them is returned. Today's visit stays. */
    @Transaction
    open fun removeUpcoming(today: String): DoctorVisitLocalModel? = firstUpcoming(today)?.also {
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
