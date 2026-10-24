package com.migrely.app.datasource.medication.model

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * The last "Got it" on an overuse warning (database version 4, BRD EC-MED-06). [category] is `SIMPLE`, `TRIPTAN`,
 * `COMBINATION_OTHER` or `MULTIPLE`; [level] is `SOFT` or `DOCTOR`.
 */
@Entity(tableName = "overuse_notice", primaryKeys = ["category", "level"])
data class OveruseNoticeLocalModel(
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "level") val level: String,
    @ColumnInfo(name = "acknowledged_epoch_millis") val acknowledgedEpochMillis: Long
)
