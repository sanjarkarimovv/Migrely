package com.migrely.app.datasource.daymark.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The marks of one date (database version 5, BRD D47). [date] is ISO `yyyy-MM-dd`, so ranges compare as text. A row
 * exists only while one mark is on.
 */
@Entity(tableName = "day_mark")
data class DayMarkLocalModel(
    @PrimaryKey @ColumnInfo(name = "date") val date: String,
    @ColumnInfo(name = "period", defaultValue = "0") val period: Boolean,
    @ColumnInfo(name = "pain_free", defaultValue = "0") val painFree: Boolean,
    @ColumnInfo(name = "updated_epoch_millis") val updatedEpochMillis: Long
)
