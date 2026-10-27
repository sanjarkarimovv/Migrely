package com.migrely.app.datasource.backupfile.model

import kotlinx.serialization.Serializable

/**
 * A backup file (BRD D71). Times are epoch milliseconds, zones are IDs, and kinds and keys are the ones the database
 * stores. Every field added after the first version has a default, so older files still read (BRD EC-BAK-03).
 */
@Serializable
data class BackupFileLocalModel(
    val format: String,
    val schemaVersion: Int,
    val exportedAt: String,
    val attacks: List<BackupFileAttackLocalModel> = emptyList(),
    val customTags: List<BackupFileCustomTagLocalModel> = emptyList(),
    val medications: List<BackupFileMedicationLocalModel> = emptyList(),
    val intakes: List<BackupFileIntakeLocalModel> = emptyList(),
    val dayMarks: List<BackupFileDayMarkLocalModel> = emptyList()
)

/** Read first, so a file from a newer version is told apart from one that is not a backup at all. */
@Serializable
data class BackupFileHeaderLocalModel(val format: String? = null, val schemaVersion: Int? = null)

@Serializable
data class BackupFileAttackLocalModel(
    val id: String,
    val startEpochMillis: Long,
    val startZoneId: String,
    val endEpochMillis: Long? = null,
    val intensityPoints: List<BackupFileIntensityPointLocalModel> = emptyList(),
    val longCheckConfirmedEpochMillis: Long? = null,
    val longCheckDisabled: Boolean = false,
    val tags: List<BackupFileTagLocalModel> = emptyList(),
    val functionalImpact: Int? = null,
    val note: String? = null,
    val createdEpochMillis: Long,
    val updatedEpochMillis: Long
)

@Serializable
data class BackupFileIntensityPointLocalModel(val atEpochMillis: Long, val value: Int)

/** [kind] is `LOCATION`, `SYMPTOM` or `TRIGGER`; [key] is a built-in key or `custom:<tag id>`. */
@Serializable
data class BackupFileTagLocalModel(val kind: String, val key: String)

@Serializable
data class BackupFileCustomTagLocalModel(
    val id: String,
    val kind: String,
    val name: String,
    val archived: Boolean = false,
    val createdEpochMillis: Long
)

@Serializable
data class BackupFileMedicationLocalModel(
    val id: String,
    val name: String,
    val kind: String,
    val category: String? = null,
    val defaultDose: String? = null,
    val archived: Boolean = false,
    val createdEpochMillis: Long,
    val updatedEpochMillis: Long
)

@Serializable
data class BackupFileIntakeLocalModel(
    val id: String,
    val medicationId: String,
    val attackId: String? = null,
    val takenEpochMillis: Long,
    val takenZoneId: String,
    val dose: String? = null,
    val effect: String? = null,
    val effectClosed: Boolean = false,
    val createdEpochMillis: Long
)

/** [date] is ISO `yyyy-MM-dd`. */
@Serializable
data class BackupFileDayMarkLocalModel(
    val date: String,
    val period: Boolean = false,
    val painFree: Boolean = false,
    val updatedEpochMillis: Long
)
