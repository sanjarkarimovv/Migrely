package com.migrely.app.datasource.daymark.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.migrely.app.datasource.daymark.model.DayMarkLocalModel

/** Day mark queries (database version 5). */
@Dao
abstract class DayMarkDao {
    @Query("SELECT * FROM day_mark WHERE date BETWEEN :from AND :to ORDER BY date")
    abstract fun between(from: String, to: String): List<DayMarkLocalModel>

    @Query("SELECT MIN(date) FROM day_mark")
    abstract fun earliestDate(): String?

    @Query("SELECT * FROM day_mark WHERE date = :date")
    abstract fun byDate(date: String): DayMarkLocalModel?

    @Upsert
    abstract fun upsert(mark: DayMarkLocalModel)

    @Query("DELETE FROM day_mark WHERE date = :date")
    abstract fun delete(date: String)

    /** Changes one flag of [date]; the row goes when no mark is left (design D1). */
    @Transaction
    open fun update(
        date: String,
        nowEpochMillis: Long,
        change: (DayMarkLocalModel) -> DayMarkLocalModel
    ) {
        val current = byDate(date) ?: DayMarkLocalModel(date, period = false, painFree = false, 0L)
        val changed = change(current).copy(updatedEpochMillis = nowEpochMillis)
        if (changed.period || changed.painFree) upsert(changed) else delete(date)
    }
}
