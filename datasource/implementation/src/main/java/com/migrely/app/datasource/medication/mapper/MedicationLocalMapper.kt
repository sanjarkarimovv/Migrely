package com.migrely.app.datasource.medication.mapper

import com.migrely.app.datasource.medication.model.AcuteIntakeDataModel
import com.migrely.app.datasource.medication.model.AcuteIntakeLocalModel
import com.migrely.app.datasource.medication.model.IntakeDataModel
import com.migrely.app.datasource.medication.model.IntakeEffectDataModel
import com.migrely.app.datasource.medication.model.IntakeWithMedicationLocalModel
import com.migrely.app.datasource.medication.model.MedicationCategoryDataModel
import com.migrely.app.datasource.medication.model.MedicationDataModel
import com.migrely.app.datasource.medication.model.MedicationKindDataModel
import com.migrely.app.datasource.medication.model.MedicationLocalModel
import com.migrely.app.datasource.medication.model.PendingEffectQuestionDataModel
import com.migrely.app.datasource.medication.model.PendingEffectQuestionLocalModel
import java.time.Instant

private const val ACUTE = "ACUTE"
private const val PREVENTIVE = "PREVENTIVE"
private const val SIMPLE = "SIMPLE"
private const val TRIPTAN = "TRIPTAN"
private const val COMBINATION_OTHER = "COMBINATION_OTHER"
private const val NOT_AT_ALL = "NOT_AT_ALL"
private const val A_LITTLE = "A_LITTLE"
private const val A_LOT = "A_LOT"
private const val COMPLETELY = "COMPLETELY"

/**
 * Stored keys never change. A kind this version does not know reads as acute, an unknown category as "Not sure" and an
 * unknown effect as no answer, so a newer database never hides a medication from an older app.
 */
class MedicationLocalMapper {
    fun toData(local: MedicationLocalModel) = MedicationDataModel(
        id = local.id,
        name = local.name,
        kind = if (local.kind ==
            PREVENTIVE
        ) {
            MedicationKindDataModel.Preventive
        } else {
            MedicationKindDataModel.Acute
        },
        category = toDataCategory(local.category),
        defaultDose = local.defaultDose,
        archived = local.archived
    )

    fun toData(local: IntakeWithMedicationLocalModel) = IntakeDataModel(
        id = local.id,
        medicationId = local.medicationId,
        medicationName = local.medicationName,
        attackId = local.attackId,
        takenAt = Instant.ofEpochMilli(local.takenEpochMillis),
        takenZoneId = local.takenZoneId,
        dose = local.dose,
        effect = toDataEffect(local.effect),
        effectClosed = local.effectClosed
    )

    fun toData(local: PendingEffectQuestionLocalModel) = PendingEffectQuestionDataModel(
        intakeId = local.id,
        medicationName = local.medicationName,
        dose = local.dose,
        takenAt = Instant.ofEpochMilli(local.takenEpochMillis),
        takenZoneId = local.takenZoneId
    )

    fun toData(local: AcuteIntakeLocalModel) = AcuteIntakeDataModel(
        takenAt = Instant.ofEpochMilli(local.takenEpochMillis),
        takenZoneId = local.takenZoneId,
        category = toDataCategory(local.category)
    )

    fun toLocalKind(kind: MedicationKindDataModel): String = when (kind) {
        MedicationKindDataModel.Acute -> ACUTE
        MedicationKindDataModel.Preventive -> PREVENTIVE
    }

    fun toLocalCategory(category: MedicationCategoryDataModel?): String? = when (category) {
        MedicationCategoryDataModel.SimplePainkiller -> SIMPLE
        MedicationCategoryDataModel.Triptan -> TRIPTAN
        MedicationCategoryDataModel.CombinationOrOther -> COMBINATION_OTHER
        null -> null
    }

    fun toLocalEffect(effect: IntakeEffectDataModel?): String? = when (effect) {
        IntakeEffectDataModel.NotAtAll -> NOT_AT_ALL
        IntakeEffectDataModel.ALittle -> A_LITTLE
        IntakeEffectDataModel.ALot -> A_LOT
        IntakeEffectDataModel.Completely -> COMPLETELY
        null -> null
    }

    private fun toDataCategory(category: String?) = when (category) {
        SIMPLE -> MedicationCategoryDataModel.SimplePainkiller
        TRIPTAN -> MedicationCategoryDataModel.Triptan
        COMBINATION_OTHER -> MedicationCategoryDataModel.CombinationOrOther
        else -> null
    }

    private fun toDataEffect(effect: String?) = when (effect) {
        NOT_AT_ALL -> IntakeEffectDataModel.NotAtAll
        A_LITTLE -> IntakeEffectDataModel.ALittle
        A_LOT -> IntakeEffectDataModel.ALot
        COMPLETELY -> IntakeEffectDataModel.Completely
        else -> null
    }
}
