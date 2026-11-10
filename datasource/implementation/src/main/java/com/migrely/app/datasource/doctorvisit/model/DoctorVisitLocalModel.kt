package com.migrely.app.datasource.doctorvisit.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A doctor visit (database version 7, BRD D168). [date] is ISO `yyyy-MM-dd` with no zone, like a day mark, so ranges
 * compare as text, and one date holds one visit.
 */
@Entity(tableName = "doctor_visit", indices = [Index(value = ["date"], unique = true)])
data class DoctorVisitLocalModel(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "date") val date: String
)
