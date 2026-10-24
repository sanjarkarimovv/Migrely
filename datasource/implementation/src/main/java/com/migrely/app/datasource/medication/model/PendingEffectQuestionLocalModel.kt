package com.migrely.app.datasource.medication.model

import androidx.room.ColumnInfo

/** An intake waiting for "Did it help?", read with its medication's name. */
data class PendingEffectQuestionLocalModel(
    @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "medication_name") val medicationName: String,
    @ColumnInfo(name = "dose") val dose: String?,
    @ColumnInfo(name = "taken_epoch_millis") val takenEpochMillis: Long,
    @ColumnInfo(name = "taken_zone_id") val takenZoneId: String
)
