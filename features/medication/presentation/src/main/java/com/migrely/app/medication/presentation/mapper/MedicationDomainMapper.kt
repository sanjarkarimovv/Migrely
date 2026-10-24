package com.migrely.app.medication.presentation.mapper

import com.migrely.app.medication.domain.model.MedicationCategoryDomainModel
import com.migrely.app.medication.domain.model.MedicationKindDomainModel
import com.migrely.app.medication.domain.model.SaveMedicationRequestDomainModel
import com.migrely.app.medication.presentation.model.MedicationCategoryPresentationModel
import com.migrely.app.medication.presentation.model.MedicationKindPresentationModel

/** Builds the save request from what the user entered. */
class MedicationDomainMapper {
    fun toDomain(
        medicationId: String?,
        name: String,
        kind: MedicationKindPresentationModel,
        category: MedicationCategoryPresentationModel?,
        defaultDose: String?
    ) = SaveMedicationRequestDomainModel(
        id = medicationId,
        name = name,
        kind = when (kind) {
            MedicationKindPresentationModel.Acute -> MedicationKindDomainModel.Acute
            MedicationKindPresentationModel.Preventive -> MedicationKindDomainModel.Preventive
        },
        category = when (category) {
            MedicationCategoryPresentationModel.SimplePainkiller ->
                MedicationCategoryDomainModel.SimplePainkiller
            MedicationCategoryPresentationModel.Triptan -> MedicationCategoryDomainModel.Triptan
            MedicationCategoryPresentationModel.CombinationOrOther ->
                MedicationCategoryDomainModel.CombinationOrOther
            null -> null
        },
        defaultDose = defaultDose
    )
}
