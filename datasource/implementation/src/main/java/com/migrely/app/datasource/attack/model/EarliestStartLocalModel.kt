package com.migrely.app.datasource.attack.model

import androidx.room.ColumnInfo

/** The start of the earliest attack, read without its other columns. */
data class EarliestStartLocalModel(
    @ColumnInfo(name = "start_epoch_millis") val startEpochMillis: Long,
    @ColumnInfo(name = "start_zone_id") val startZoneId: String
)
