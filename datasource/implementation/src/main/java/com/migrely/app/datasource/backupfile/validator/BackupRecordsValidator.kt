package com.migrely.app.datasource.backupfile.validator

import com.migrely.app.datasource.backup.model.BackupRecordsDataModel
import java.util.Locale

private const val MINIMUM_INTENSITY = 1
private const val MAXIMUM_INTENSITY = 10
private const val MINIMUM_IMPACT = 0
private const val MAXIMUM_IMPACT = 3

/**
 * Checks what the database and the app rely on, so a broken or hand-edited file is refused as a whole (BRD EC-BAK-01)
 * instead of failing halfway: unique IDs and names, intakes pointing at records in the file, values in range, and at
 * most one attack in progress. A backup the app wrote itself always passes.
 */
class BackupRecordsValidator {
    fun isValid(records: BackupRecordsDataModel): Boolean =
        idsAreUnique(records) && namesAreUnique(records) && attacksAreValid(records) &&
            intakesPointAtRecords(records) && records.dayMarks.all { it.period || it.painFree }

    private fun idsAreUnique(records: BackupRecordsDataModel): Boolean = listOf(
        records.attacks.map { it.id },
        records.customTags.map { it.id },
        records.medications.map { it.id },
        records.intakes.map { it.id }
    ).all { ids -> ids.none(String::isBlank) && ids.toSet().size == ids.size } &&
        records.dayMarks.map { it.date }.let { it.toSet().size == it.size }

    private fun namesAreUnique(records: BackupRecordsDataModel): Boolean {
        val tagNames = records.customTags.map { it.kind to keyOf(it.name) }
        val medicationNames = records.medications.map { keyOf(it.name) }
        return tagNames.none { it.second.isEmpty() } && tagNames.toSet().size == tagNames.size &&
            medicationNames.none(String::isEmpty) &&
            medicationNames.toSet().size == medicationNames.size
    }

    private fun attacksAreValid(records: BackupRecordsDataModel): Boolean =
        records.attacks.count { it.end == null } <= 1 &&
            records.attacks.all { attack ->
                attack.end?.isBefore(attack.start) != true &&
                    attack.intensityPoints.all {
                        it.value in MINIMUM_INTENSITY..MAXIMUM_INTENSITY
                    } &&
                    attack.functionalImpact?.let {
                        it in MINIMUM_IMPACT..MAXIMUM_IMPACT
                    } != false &&
                    attack.tags.toSet().size == attack.tags.size
            }

    private fun intakesPointAtRecords(records: BackupRecordsDataModel): Boolean {
        val medicationIds = records.medications.map { it.id }.toSet()
        val attackIds = records.attacks.map { it.id }.toSet()
        return records.intakes.all { intake ->
            intake.medicationId in medicationIds &&
                (intake.attackId == null || intake.attackId in attackIds)
        }
    }

    private fun keyOf(name: String) = name.trim().lowercase(Locale.ROOT)
}
