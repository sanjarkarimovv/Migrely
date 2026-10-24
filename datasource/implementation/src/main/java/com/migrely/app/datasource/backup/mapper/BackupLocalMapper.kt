package com.migrely.app.datasource.backup.mapper

import com.migrely.app.datasource.attack.mapper.AttackTagLocalMapper
import com.migrely.app.datasource.attack.model.AttackLocalModel
import com.migrely.app.datasource.attack.model.AttackTagDataModel
import com.migrely.app.datasource.attack.model.IntensityPointDataModel
import com.migrely.app.datasource.attack.model.IntensityPointLocalModel
import com.migrely.app.datasource.backup.model.BackupAttackDataModel
import com.migrely.app.datasource.backup.model.BackupCustomTagDataModel
import com.migrely.app.datasource.backup.model.BackupDayMarkDataModel
import com.migrely.app.datasource.backup.model.BackupIntakeDataModel
import com.migrely.app.datasource.backup.model.BackupLocalModel
import com.migrely.app.datasource.backup.model.BackupMedicationDataModel
import com.migrely.app.datasource.backup.model.BackupRecordsDataModel
import com.migrely.app.datasource.daymark.mapper.DayMarkLocalMapper
import com.migrely.app.datasource.daymark.model.DayMarkLocalModel
import com.migrely.app.datasource.medication.mapper.MedicationLocalMapper
import com.migrely.app.datasource.medication.model.IntakeEffectDataModel
import com.migrely.app.datasource.medication.model.MedicationIntakeLocalModel
import com.migrely.app.datasource.medication.model.MedicationLocalModel
import com.migrely.app.datasource.tag.mapper.CustomTagLocalMapper
import com.migrely.app.datasource.tag.model.CustomTagKindDataModel
import com.migrely.app.datasource.tag.model.CustomTagLocalModel
import java.time.Instant
import java.util.Locale

/**
 * Turns every stored health row into backup records and back. Rows this version cannot read are left out, as on every
 * other screen. Name keys follow the rule the app saves names by: trimmed, then lower-cased (BRD EC-MED-07).
 */
class BackupLocalMapper(
    private val attackTagLocalMapper: AttackTagLocalMapper,
    private val customTagLocalMapper: CustomTagLocalMapper,
    private val medicationLocalMapper: MedicationLocalMapper,
    private val dayMarkLocalMapper: DayMarkLocalMapper
) {
    fun toData(local: BackupLocalModel): BackupRecordsDataModel {
        val pointsByAttack = local.intensityPoints.groupBy(IntensityPointLocalModel::attackId)
        val tagsByAttack = local.attackTags.groupBy { it.attackId }
        return BackupRecordsDataModel(
            attacks = local.attacks.map { attack ->
                toData(
                    attack,
                    pointsByAttack[attack.id].orEmpty(),
                    tagsByAttack[attack.id].orEmpty().mapNotNull(attackTagLocalMapper::toData)
                )
            },
            customTags = local.customTags.mapNotNull(::toData),
            medications = local.medications.map(::toData),
            intakes = local.intakes.map(::toData),
            dayMarks = local.dayMarks.mapNotNull(::toData)
        )
    }

    fun toLocal(records: BackupRecordsDataModel) = BackupLocalModel(
        attacks = records.attacks.map(::toLocal),
        intensityPoints = records.attacks.flatMap { attack ->
            attack.intensityPoints.map {
                IntensityPointLocalModel(
                    attackId = attack.id,
                    atEpochMillis = it.at.toEpochMilli(),
                    value = it.value
                )
            }
        },
        attackTags = records.attacks.flatMap { attack ->
            attack.tags.map { attackTagLocalMapper.toLocal(attack.id, it) }
        },
        customTags = records.customTags.map(::toLocal),
        medications = records.medications.map(::toLocal),
        intakes = records.intakes.map(::toLocal),
        dayMarks = records.dayMarks.map(::toLocal)
    )

    private fun toData(
        attack: AttackLocalModel,
        points: List<IntensityPointLocalModel>,
        tags: List<AttackTagDataModel>
    ) = BackupAttackDataModel(
        id = attack.id,
        start = Instant.ofEpochMilli(attack.startEpochMillis),
        startZoneId = attack.startZoneId,
        end = attack.endEpochMillis?.let(Instant::ofEpochMilli),
        intensityPoints = points.map {
            IntensityPointDataModel(Instant.ofEpochMilli(it.atEpochMillis), it.value)
        },
        longCheckConfirmedAt = attack.longCheckConfirmedEpochMillis?.let(Instant::ofEpochMilli),
        longCheckDisabled = attack.longCheckDisabled,
        tags = tags,
        functionalImpact = attack.functionalImpact,
        note = attack.note,
        createdAt = Instant.ofEpochMilli(attack.createdEpochMillis),
        updatedAt = Instant.ofEpochMilli(attack.updatedEpochMillis)
    )

    private fun toData(tag: CustomTagLocalModel): BackupCustomTagDataModel? =
        CustomTagKindDataModel.entries.firstOrNull {
            customTagLocalMapper.toLocalKind(it) ==
                tag.kind
        }
            ?.let { kind ->
                BackupCustomTagDataModel(
                    id = tag.id,
                    kind = kind,
                    name = tag.name,
                    archived = tag.archived,
                    createdAt = Instant.ofEpochMilli(tag.createdEpochMillis)
                )
            }

    private fun toData(medication: MedicationLocalModel): BackupMedicationDataModel {
        val read = medicationLocalMapper.toData(medication)
        return BackupMedicationDataModel(
            id = medication.id,
            name = medication.name,
            kind = read.kind,
            category = read.category,
            defaultDose = medication.defaultDose,
            archived = medication.archived,
            createdAt = Instant.ofEpochMilli(medication.createdEpochMillis),
            updatedAt = Instant.ofEpochMilli(medication.updatedEpochMillis)
        )
    }

    private fun toData(intake: MedicationIntakeLocalModel) = BackupIntakeDataModel(
        id = intake.id,
        medicationId = intake.medicationId,
        attackId = intake.attackId,
        takenAt = Instant.ofEpochMilli(intake.takenEpochMillis),
        takenZoneId = intake.takenZoneId,
        dose = intake.dose,
        effect = IntakeEffectDataModel.entries.firstOrNull {
            medicationLocalMapper.toLocalEffect(it) == intake.effect
        },
        effectClosed = intake.effectClosed,
        createdAt = Instant.ofEpochMilli(intake.createdEpochMillis)
    )

    private fun toData(mark: DayMarkLocalModel): BackupDayMarkDataModel? =
        dayMarkLocalMapper.toDate(mark.date)?.let {
            BackupDayMarkDataModel(
                date = it,
                period = mark.period,
                painFree = mark.painFree,
                updatedAt = Instant.ofEpochMilli(mark.updatedEpochMillis)
            )
        }

    private fun toLocal(attack: BackupAttackDataModel) = AttackLocalModel(
        id = attack.id,
        startEpochMillis = attack.start.toEpochMilli(),
        startZoneId = attack.startZoneId,
        endEpochMillis = attack.end?.toEpochMilli(),
        longCheckConfirmedEpochMillis = attack.longCheckConfirmedAt?.toEpochMilli(),
        longCheckDisabled = attack.longCheckDisabled,
        createdEpochMillis = attack.createdAt.toEpochMilli(),
        updatedEpochMillis = attack.updatedAt.toEpochMilli(),
        functionalImpact = attack.functionalImpact,
        note = attack.note
    )

    private fun toLocal(tag: BackupCustomTagDataModel) = CustomTagLocalModel(
        id = tag.id,
        kind = customTagLocalMapper.toLocalKind(tag.kind),
        name = tag.name,
        nameKey = nameKeyOf(tag.name),
        archived = tag.archived,
        createdEpochMillis = tag.createdAt.toEpochMilli()
    )

    private fun toLocal(medication: BackupMedicationDataModel) = MedicationLocalModel(
        id = medication.id,
        name = medication.name,
        nameKey = nameKeyOf(medication.name),
        kind = medicationLocalMapper.toLocalKind(medication.kind),
        category = medicationLocalMapper.toLocalCategory(medication.category),
        defaultDose = medication.defaultDose,
        archived = medication.archived,
        createdEpochMillis = medication.createdAt.toEpochMilli(),
        updatedEpochMillis = medication.updatedAt.toEpochMilli()
    )

    private fun toLocal(intake: BackupIntakeDataModel) = MedicationIntakeLocalModel(
        id = intake.id,
        medicationId = intake.medicationId,
        attackId = intake.attackId,
        takenEpochMillis = intake.takenAt.toEpochMilli(),
        takenZoneId = intake.takenZoneId,
        dose = intake.dose,
        createdEpochMillis = intake.createdAt.toEpochMilli(),
        effect = medicationLocalMapper.toLocalEffect(intake.effect),
        effectClosed = intake.effectClosed
    )

    private fun toLocal(mark: BackupDayMarkDataModel) = DayMarkLocalModel(
        date = dayMarkLocalMapper.toLocal(mark.date),
        period = mark.period,
        painFree = mark.painFree,
        updatedEpochMillis = mark.updatedAt.toEpochMilli()
    )

    private fun nameKeyOf(name: String) = name.trim().lowercase(Locale.ROOT)
}
