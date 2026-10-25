package com.migrely.app.datasource.medication.model

import androidx.room.ColumnInfo

/** When and where the earliest acute intake was taken, read without its other columns. */
data class EarliestAcuteIntakeLocalModel(
    @ColumnInfo(name = "taken_epoch_millis") val takenEpochMillis: Long,
    @ColumnInfo(name = "taken_zone_id") val takenZoneId: String
)
