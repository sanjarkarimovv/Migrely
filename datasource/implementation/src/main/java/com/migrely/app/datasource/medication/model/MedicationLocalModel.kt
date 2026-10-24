package com.migrely.app.datasource.medication.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A medication (database version 3). [kind] is `ACUTE` or `PREVENTIVE`; [category] is `SIMPLE`, `TRIPTAN`,
 * `COMBINATION_OTHER` or null; [nameKey] is the trimmed, lower-cased name and is unique (BRD EC-MED-07).
 */
@Entity(tableName = "medication", indices = [Index("name_key", unique = true)])
data class MedicationLocalModel(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "name_key") val nameKey: String,
    @ColumnInfo(name = "kind") val kind: String,
    @ColumnInfo(name = "category") val category: String?,
    @ColumnInfo(name = "default_dose") val defaultDose: String?,
    @ColumnInfo(name = "archived") val archived: Boolean,
    @ColumnInfo(name = "created_epoch_millis") val createdEpochMillis: Long,
    @ColumnInfo(name = "updated_epoch_millis") val updatedEpochMillis: Long
)
