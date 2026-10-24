package com.migrely.app.datasource.attack.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attack",
    indices = [Index("start_epoch_millis"), Index("end_epoch_millis")]
)
data class AttackLocalModel(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "start_epoch_millis") val startEpochMillis: Long,
    @ColumnInfo(name = "start_zone_id") val startZoneId: String,
    @ColumnInfo(name = "end_epoch_millis") val endEpochMillis: Long?,
    @ColumnInfo(name = "long_check_confirmed_epoch_millis") val longCheckConfirmedEpochMillis:
    Long?,
    @ColumnInfo(name = "long_check_disabled") val longCheckDisabled: Boolean,
    @ColumnInfo(name = "created_epoch_millis") val createdEpochMillis: Long,
    @ColumnInfo(name = "updated_epoch_millis") val updatedEpochMillis: Long
)
