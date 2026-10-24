package com.migrely.app.datasource.medication.model

import androidx.room.ColumnInfo

/** An acute intake with its medication's category, for counting overuse days. */
data class AcuteIntakeLocalModel(
    @ColumnInfo(name = "taken_epoch_millis") val takenEpochMillis: Long,
    @ColumnInfo(name = "taken_zone_id") val takenZoneId: String,
    @ColumnInfo(name = "category") val category: String?
)
