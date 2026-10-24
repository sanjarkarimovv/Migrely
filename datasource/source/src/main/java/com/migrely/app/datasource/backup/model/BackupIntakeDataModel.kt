package com.migrely.app.datasource.backup.model

import com.migrely.app.datasource.medication.model.IntakeEffectDataModel
import java.time.Instant

data class BackupIntakeDataModel(
    val id: String,
    val medicationId: String,
    val attackId: String?,
    val takenAt: Instant,
    val takenZoneId: String,
    val dose: String?,
    val effect: IntakeEffectDataModel?,
    val effectClosed: Boolean,
    val createdAt: Instant
)
