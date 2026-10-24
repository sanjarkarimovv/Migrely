package com.migrely.app.datasource.medication.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.migrely.app.datasource.attack.model.AttackLocalModel

/**
 * A medication taken (database version 3; [effect] and [effectClosed] since version 4). Medications are never deleted, so the medication key restricts; an attack
 * removed for good takes its intakes with it (BRD EC-MED-10).
 */
@Entity(
    tableName = "medication_intake",
    foreignKeys = [
        ForeignKey(
            entity = MedicationLocalModel::class,
            parentColumns = ["id"],
            childColumns = ["medication_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = AttackLocalModel::class,
            parentColumns = ["id"],
            childColumns = ["attack_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("medication_id"), Index("attack_id"), Index("taken_epoch_millis")]
)
data class MedicationIntakeLocalModel(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "medication_id") val medicationId: String,
    @ColumnInfo(name = "attack_id") val attackId: String?,
    @ColumnInfo(name = "taken_epoch_millis") val takenEpochMillis: Long,
    @ColumnInfo(name = "taken_zone_id") val takenZoneId: String,
    @ColumnInfo(name = "dose") val dose: String?,
    @ColumnInfo(name = "created_epoch_millis") val createdEpochMillis: Long,
    /** `NOT_AT_ALL`, `A_LITTLE`, `A_LOT`, `COMPLETELY` or null (BRD D39). */
    @ColumnInfo(name = "effect") val effect: String? = null,
    @ColumnInfo(name = "effect_closed", defaultValue = "0") val effectClosed: Boolean = false
)
