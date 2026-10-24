package com.migrely.app.datasource.tag.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** A symptom or trigger the user added. [kind] is `SYMPTOM` or `TRIGGER`; [nameKey] is the trimmed, lower-cased name. */
@Entity(
    tableName = "custom_tag",
    indices = [Index("kind", "name_key", unique = true)]
)
data class CustomTagLocalModel(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "kind") val kind: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "name_key") val nameKey: String,
    @ColumnInfo(name = "archived") val archived: Boolean,
    @ColumnInfo(name = "created_epoch_millis") val createdEpochMillis: Long
)
