package com.migrely.app.datasource.medication.mapper

import com.migrely.app.datasource.medication.model.IntakeDataModel
import com.migrely.app.datasource.medication.model.IntakeWithMedicationLocalModel
import com.migrely.app.datasource.medication.model.MedicationCategoryDataModel
import com.migrely.app.datasource.medication.model.MedicationDataModel
import com.migrely.app.datasource.medication.model.MedicationKindDataModel
import com.migrely.app.datasource.medication.model.MedicationLocalModel
import java.time.Instant

private const val ACUTE = "ACUTE"
private const val PREVENTIVE = "PREVENTIVE"
private const val SIMPLE = "SIMPLE"
private const val TRIPTAN = "TRIPTAN"
private const val COMBINATION_OTHER = "COMBINATION_OTHER"

/**
 * Stored keys never change. A kind this version does not know reads as acute and an unknown category as "Not sure", so
 * a newer database never hides a medication from an older app.
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
        category = when (local.category) {
            SIMPLE -> MedicationCategoryDataModel.SimplePainkiller
            TRIPTAN -> MedicationCategoryDataModel.Triptan
            COMBINATION_OTHER -> MedicationCategoryDataModel.CombinationOrOther
            else -> null
        },
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
        dose = local.dose
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
}
