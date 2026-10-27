package com.migrely.app.datasource.attack.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * A location, symptom or trigger selected on an attack. [kind] is `LOCATION`, `SYMPTOM` or `TRIGGER`; [tagKey] is a
 * built-in key (for example `aura`) or `custom:<tag id>`.
 */
@Entity(
    tableName = "attack_tag",
    primaryKeys = ["attack_id", "kind", "tag_key"],
    foreignKeys = [
        ForeignKey(
            entity = AttackLocalModel::class,
            parentColumns = ["id"],
            childColumns = ["attack_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("kind", "tag_key")]
)
data class AttackTagLocalModel(
    @ColumnInfo(name = "attack_id") val attackId: String,
    @ColumnInfo(name = "kind") val kind: String,
    @ColumnInfo(name = "tag_key") val tagKey: String
) {
    companion object {
        /** Starts [tagKey] for a custom tag, followed by its ID. */
        const val CUSTOM_KEY_PREFIX = "custom:"
    }
}
