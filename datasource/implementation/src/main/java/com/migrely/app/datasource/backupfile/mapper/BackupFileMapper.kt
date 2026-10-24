package com.migrely.app.datasource.backupfile.mapper

import com.migrely.app.datasource.attack.mapper.AttackTagLocalMapper
import com.migrely.app.datasource.attack.model.AttackTagDataModel
import com.migrely.app.datasource.attack.model.AttackTagLocalModel
import com.migrely.app.datasource.attack.model.IntensityPointDataModel
import com.migrely.app.datasource.backup.model.BackupAttackDataModel
import com.migrely.app.datasource.backup.model.BackupCustomTagDataModel
import com.migrely.app.datasource.backup.model.BackupDayMarkDataModel
import com.migrely.app.datasource.backup.model.BackupIntakeDataModel
import com.migrely.app.datasource.backup.model.BackupMedicationDataModel
import com.migrely.app.datasource.backup.model.BackupRecordsDataModel
import com.migrely.app.datasource.backupfile.model.BackupFileAttackLocalModel
import com.migrely.app.datasource.backupfile.model.BackupFileCustomTagLocalModel
import com.migrely.app.datasource.backupfile.model.BackupFileDayMarkLocalModel
import com.migrely.app.datasource.backupfile.model.BackupFileIntakeLocalModel
import com.migrely.app.datasource.backupfile.model.BackupFileIntensityPointLocalModel
import com.migrely.app.datasource.backupfile.model.BackupFileLocalModel
import com.migrely.app.datasource.backupfile.model.BackupFileMedicationLocalModel
import com.migrely.app.datasource.backupfile.model.BackupFileTagLocalModel
import com.migrely.app.datasource.medication.mapper.MedicationLocalMapper
import com.migrely.app.datasource.medication.model.IntakeEffectDataModel
import com.migrely.app.datasource.medication.model.MedicationCategoryDataModel
import com.migrely.app.datasource.medication.model.MedicationKindDataModel
import com.migrely.app.datasource.tag.mapper.CustomTagLocalMapper
import com.migrely.app.datasource.tag.model.CustomTagKindDataModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Turns backup records into the file format and back, with the keys the database stores. Reading is strict: a kind,
 * key, zone or date this version does not know throws [IllegalArgumentException] or [java.time.DateTimeException], so
 * it never reaches the database. Names are trimmed as the app saves them.
 */
class BackupFileMapper(
    private val attackTagLocalMapper: AttackTagLocalMapper,
    private val customTagLocalMapper: CustomTagLocalMapper,
    private val medicationLocalMapper: MedicationLocalMapper
) {
    fun toLocal(records: BackupRecordsDataModel, header: Header) = BackupFileLocalModel(
        format = header.format,
        schemaVersion = header.schemaVersion,
        exportedAt = header.exportedAt.toString(),
        attacks = records.attacks.map(::toLocal),
        customTags = records.customTags.map(::toLocal),
        medications = records.medications.map(::toLocal),
        intakes = records.intakes.map(::toLocal),
        dayMarks = records.dayMarks.map(::toLocal)
    )

    fun toData(file: BackupFileLocalModel) = BackupRecordsDataModel(
        attacks = file.attacks.map(::toData),
        customTags = file.customTags.map(::toData),
        medications = file.medications.map(::toData),
        intakes = file.intakes.map(::toData),
        dayMarks = file.dayMarks.map(::toData)
    )

    data class Header(val format: String, val schemaVersion: Int, val exportedAt: Instant)

    private fun toLocal(attack: BackupAttackDataModel) = BackupFileAttackLocalModel(
        id = attack.id,
        startEpochMillis = attack.start.toEpochMilli(),
        startZoneId = attack.startZoneId,
        endEpochMillis = attack.end?.toEpochMilli(),
        intensityPoints = attack.intensityPoints.map {
            BackupFileIntensityPointLocalModel(it.at.toEpochMilli(), it.value)
        },
        longCheckConfirmedEpochMillis = attack.longCheckConfirmedAt?.toEpochMilli(),
        longCheckDisabled = attack.longCheckDisabled,
        tags = attack.tags.map { tag ->
            attackTagLocalMapper.toLocal(attack.id, tag).let {
                BackupFileTagLocalModel(it.kind, it.tagKey)
            }
        },
        functionalImpact = attack.functionalImpact,
        note = attack.note,
        createdEpochMillis = attack.createdAt.toEpochMilli(),
        updatedEpochMillis = attack.updatedAt.toEpochMilli()
    )

    private fun toLocal(tag: BackupCustomTagDataModel) = BackupFileCustomTagLocalModel(
        id = tag.id,
        kind = customTagLocalMapper.toLocalKind(tag.kind),
        name = tag.name,
        archived = tag.archived,
        createdEpochMillis = tag.createdAt.toEpochMilli()
    )

    private fun toLocal(medication: BackupMedicationDataModel) = BackupFileMedicationLocalModel(
        id = medication.id,
        name = medication.name,
        kind = medicationLocalMapper.toLocalKind(medication.kind),
        category = medicationLocalMapper.toLocalCategory(medication.category),
        defaultDose = medication.defaultDose,
        archived = medication.archived,
        createdEpochMillis = medication.createdAt.toEpochMilli(),
        updatedEpochMillis = medication.updatedAt.toEpochMilli()
    )

    private fun toLocal(intake: BackupIntakeDataModel) = BackupFileIntakeLocalModel(
        id = intake.id,
        medicationId = intake.medicationId,
        attackId = intake.attackId,
        takenEpochMillis = intake.takenAt.toEpochMilli(),
        takenZoneId = intake.takenZoneId,
        dose = intake.dose,
        effect = medicationLocalMapper.toLocalEffect(intake.effect),
        effectClosed = intake.effectClosed,
        createdEpochMillis = intake.createdAt.toEpochMilli()
    )

    private fun toLocal(mark: BackupDayMarkDataModel) = BackupFileDayMarkLocalModel(
        date = mark.date.toString(),
        period = mark.period,
        painFree = mark.painFree,
        updatedEpochMillis = mark.updatedAt.toEpochMilli()
    )

    private fun toData(attack: BackupFileAttackLocalModel) = BackupAttackDataModel(
        id = attack.id,
        start = Instant.ofEpochMilli(attack.startEpochMillis),
        startZoneId = zone(attack.startZoneId),
        end = attack.endEpochMillis?.let(Instant::ofEpochMilli),
        intensityPoints = attack.intensityPoints.map {
            IntensityPointDataModel(Instant.ofEpochMilli(it.atEpochMillis), it.value)
        },
        longCheckConfirmedAt = attack.longCheckConfirmedEpochMillis?.let(Instant::ofEpochMilli),
        longCheckDisabled = attack.longCheckDisabled,
        tags = attack.tags.map { toData(attack.id, it) },
        functionalImpact = attack.functionalImpact,
        note = attack.note,
        createdAt = Instant.ofEpochMilli(attack.createdEpochMillis),
        updatedAt = Instant.ofEpochMilli(attack.updatedEpochMillis)
    )

    private fun toData(attackId: String, tag: BackupFileTagLocalModel): AttackTagDataModel =
        requireNotNull(
            attackTagLocalMapper.toData(AttackTagLocalModel(attackId, tag.kind, tag.key))
        ) {
            "Unknown tag ${tag.kind} ${tag.key}"
        }

    private fun toData(tag: BackupFileCustomTagLocalModel) = BackupCustomTagDataModel(
        id = tag.id,
        kind = known(CustomTagKindDataModel.entries, tag.kind, customTagLocalMapper::toLocalKind),
        name = tag.name.trim(),
        archived = tag.archived,
        createdAt = Instant.ofEpochMilli(tag.createdEpochMillis)
    )

    private fun toData(medication: BackupFileMedicationLocalModel) = BackupMedicationDataModel(
        id = medication.id,
        name = medication.name.trim(),
        kind = known(
            MedicationKindDataModel.entries,
            medication.kind,
            medicationLocalMapper::toLocalKind
        ),
        category = medication.category?.let {
            known(MedicationCategoryDataModel.entries, it, medicationLocalMapper::toLocalCategory)
        },
        defaultDose = medication.defaultDose,
        archived = medication.archived,
        createdAt = Instant.ofEpochMilli(medication.createdEpochMillis),
        updatedAt = Instant.ofEpochMilli(medication.updatedEpochMillis)
    )

    private fun toData(intake: BackupFileIntakeLocalModel) = BackupIntakeDataModel(
        id = intake.id,
        medicationId = intake.medicationId,
        attackId = intake.attackId,
        takenAt = Instant.ofEpochMilli(intake.takenEpochMillis),
        takenZoneId = zone(intake.takenZoneId),
        dose = intake.dose,
        effect = intake.effect?.let {
            known(IntakeEffectDataModel.entries, it, medicationLocalMapper::toLocalEffect)
        },
        effectClosed = intake.effectClosed,
        createdAt = Instant.ofEpochMilli(intake.createdEpochMillis)
    )

    private fun toData(mark: BackupFileDayMarkLocalModel) = BackupDayMarkDataModel(
        date = LocalDate.parse(mark.date),
        period = mark.period,
        painFree = mark.painFree,
        updatedAt = Instant.ofEpochMilli(mark.updatedEpochMillis)
    )

    /** Throws for a zone this phone does not know, as every screen would later. */
    private fun zone(zoneId: String): String = ZoneId.of(zoneId).id

    private fun <T> known(entries: List<T>, stored: String, toStored: (T) -> String?): T =
        requireNotNull(entries.firstOrNull { toStored(it) == stored }) { "Unknown key $stored" }
}
