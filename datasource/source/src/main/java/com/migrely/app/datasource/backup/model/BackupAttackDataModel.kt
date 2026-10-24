package com.migrely.app.datasource.backup.model

import com.migrely.app.datasource.attack.model.AttackTagDataModel
import com.migrely.app.datasource.attack.model.IntensityPointDataModel
import java.time.Instant

/** An attack with everything stored on it. [end] is null while it is in progress. */
data class BackupAttackDataModel(
    val id: String,
    val start: Instant,
    val startZoneId: String,
    val end: Instant?,
    val intensityPoints: List<IntensityPointDataModel>,
    val longCheckConfirmedAt: Instant?,
    val longCheckDisabled: Boolean,
    val tags: List<AttackTagDataModel>,
    val functionalImpact: Int?,
    val note: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)
