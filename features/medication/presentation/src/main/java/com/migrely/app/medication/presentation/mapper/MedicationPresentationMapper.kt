package com.migrely.app.medication.presentation.mapper

import com.migrely.app.medication.domain.model.MedicationCategoryDomainModel
import com.migrely.app.medication.domain.model.MedicationDomainModel
import com.migrely.app.medication.domain.model.MedicationKindDomainModel
import com.migrely.app.medication.presentation.model.MedicationCategoryPresentationModel
import com.migrely.app.medication.presentation.model.MedicationKindPresentationModel
import com.migrely.app.medication.presentation.model.MedicationPresentationModel

class MedicationPresentationMapper {
    fun toPresentation(medication: MedicationDomainModel) = MedicationPresentationModel(
        id = medication.id,
        name = medication.name,
        kind = when (medication.kind) {
            MedicationKindDomainModel.Acute -> MedicationKindPresentationModel.Acute
            MedicationKindDomainModel.Preventive -> MedicationKindPresentationModel.Preventive
        },
        category = when (medication.category) {
            MedicationCategoryDomainModel.SimplePainkiller ->
                MedicationCategoryPresentationModel.SimplePainkiller
            MedicationCategoryDomainModel.Triptan -> MedicationCategoryPresentationModel.Triptan
            MedicationCategoryDomainModel.CombinationOrOther ->
                MedicationCategoryPresentationModel.CombinationOrOther
            null -> null
        },
        defaultDose = medication.defaultDose
    )
}
