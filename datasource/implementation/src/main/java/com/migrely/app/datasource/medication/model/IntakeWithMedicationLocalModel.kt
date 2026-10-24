package com.migrely.app.datasource.medication.model

import androidx.room.ColumnInfo

/** An intake read together with its medication's current name. */
data class IntakeWithMedicationLocalModel(
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "medication_id") val medicationId: String,
    @ColumnInfo(name = "medication_name") val medicationName: String,
    @ColumnInfo(name = "attack_id") val attackId: String?,
    @ColumnInfo(name = "taken_epoch_millis") val takenEpochMillis: Long,
    @ColumnInfo(name = "taken_zone_id") val takenZoneId: String,
    @ColumnInfo(name = "dose") val dose: String?,
    @ColumnInfo(name = "effect") val effect: String?,
    @ColumnInfo(name = "effect_closed") val effectClosed: Boolean
)
