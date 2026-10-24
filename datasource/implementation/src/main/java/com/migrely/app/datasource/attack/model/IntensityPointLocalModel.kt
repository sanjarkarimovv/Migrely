package com.migrely.app.datasource.attack.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "intensity_point",
    foreignKeys = [
        ForeignKey(
            entity = AttackLocalModel::class,
            parentColumns = ["id"],
            childColumns = ["attack_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("attack_id")]
)
data class IntensityPointLocalModel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "attack_id") val attackId: String,
    @ColumnInfo(name = "at_epoch_millis") val atEpochMillis: Long,
    @ColumnInfo(name = "value") val value: Int
)
